import { createClient } from 'https://esm.sh/@supabase/supabase-js@2';
import { SignJWT, importPKCS8 } from 'npm:jose@6';

const cors = {'Access-Control-Allow-Origin':'*','Access-Control-Allow-Headers':'authorization, x-client-info, apikey, content-type, x-sync-secret'};
const SOURCES = [
  {name:'Kariyer Kapısı', url:'https://kariyerkapisi.gov.tr/isealim'},
  {name:'ilan.gov.tr', url:'https://www.ilan.gov.tr/'},
  {name:'Yerel Yönetimler', url:'https://yerelyonetimler.csb.gov.tr/duyurular'}
];

function clean(s:string){return s.replace(/\s+/g,' ').trim();}
function strip(html:string){return clean(html.replace(/<script[\s\S]*?<\/script>/gi,' ').replace(/<style[\s\S]*?<\/style>/gi,' ').replace(/<[^>]+>/g,' '));}
function abs(base:string,href:string){try{return new URL(href,base).toString()}catch{return href;}}
function links(html:string,base:string){
 const out:{title:string,link:string}[]=[]; const re=/<a[^>]+href=["']([^"']+)["'][^>]*>([\s\S]*?)<\/a>/gi; let m;
 while((m=re.exec(html))){const title=strip(m[2]); if(title.length>=12) out.push({title,link:abs(base,m[1])});}
 return out;
}
function score(t:string){const patterns=[/KPSS[^\d]{0,50}(\d{2,3})/i,/taban\s*puan[^\d]{0,20}(\d{2,3})/i,/en\s*az\s*(\d{2,3})\s*puan/i]; for(const p of patterns){const m=t.match(p);if(m)return Number(m[1]);}return null;}
function codes(t:string){return [...new Set((t.match(/\b\d{4}\b/g)||[]).filter(x=>/^(3001|3173|3225|4001|4003|4453|6225)$/.test(x)))];}
function contract(t:string){const x=t.toLocaleLowerCase('tr');if(x.includes('sözleşmeli'))return 'Sözleşmeli';if(x.includes('zabıta'))return 'Memur/Zabıta';if(x.includes('itfaiye'))return 'İtfaiye';if(x.includes('işçi'))return 'İşçi';return 'Memur';}
function institution(title:string,source:string){
 if(source==='Yerel Yönetimler'){const m=title.match(/(?:İLİ\s+)?(.+?)\s+BELEDİYE BAŞKANLIĞINA/i);if(m)return m[1].trim();}
 return source;
}
async function accessToken(sa:any){
 const now=Math.floor(Date.now()/1000); const key=await importPKCS8(sa.private_key,'RS256');
 const jwt=await new SignJWT({scope:'https://www.googleapis.com/auth/firebase.messaging'})
   .setProtectedHeader({alg:'RS256',typ:'JWT'}).setIssuer(sa.client_email).setAudience('https://oauth2.googleapis.com/token')
   .setIssuedAt(now).setExpirationTime(now+3600).sign(key);
 const r=await fetch('https://oauth2.googleapis.com/token',{method:'POST',headers:{'content-type':'application/x-www-form-urlencoded'},body:new URLSearchParams({grant_type:'urn:ietf:params:oauth:grant-type:jwt-bearer',assertion:jwt})});
 const j=await r.json(); if(!r.ok) throw new Error(JSON.stringify(j)); return j.access_token;
}
async function notifyNew(supabase:any,rows:any[]){
 const raw=Deno.env.get('FCM_SERVICE_ACCOUNT_JSON'); if(!raw||!rows.length)return;
 const sa=JSON.parse(raw); const token=await accessToken(sa);
 const {data:devices}=await supabase.from('device_tokens').select('token').eq('active',true);
 if(!devices?.length)return;
 for(const row of rows){for(const d of devices){
  const r=await fetch(`https://fcm.googleapis.com/v1/projects/${sa.project_id}/messages:send`,{method:'POST',headers:{Authorization:`Bearer ${token}`,'Content-Type':'application/json'},body:JSON.stringify({message:{token:d.token,notification:{title:`Yeni ilan: ${row.pozisyon}`,body:`${row.kurum} • KPSS ${row.taban_puan??'şartı ilanda'}`},data:{announcement_id:row.id,link:row.link??''}}})});
  if(!r.ok){const txt=await r.text(); console.error('FCM',txt); if(r.status===404||r.status===410) await supabase.from('device_tokens').update({active:false}).eq('token',d.token);}
 }}
}
Deno.serve(async(req)=>{
 if(req.method==='OPTIONS')return new Response('ok',{headers:cors});
 const secret=Deno.env.get('SYNC_SECRET'); if(secret&&req.headers.get('x-sync-secret')!==secret)return new Response('Unauthorized',{status:401,headers:cors});
 const supabase=createClient(Deno.env.get('SUPABASE_URL')!,Deno.env.get('SUPABASE_SERVICE_ROLE_KEY')!);
 const inserted:any[]=[];
 for(const src of SOURCES){try{
  const r=await fetch(src.url,{headers:{'User-Agent':'Mozilla/5.0 EP-Ilan/1.0','Accept':'text/html,application/xhtml+xml'}}); if(!r.ok)throw new Error(`${src.name} HTTP ${r.status}`);
  const html=await r.text();
  const candidates=links(html,src.url).filter(x=>/personel|memur|zabıta|itfaiye|sözleşmeli|kamu|akademik|işe alım|öğretim/i.test(x.title)).slice(0,300);
  for(const x of candidates){
   const externalId=btoa(unescape(encodeURIComponent(x.link))).slice(0,180);
   const {data:exists}=await supabase.from('announcements').select('id').eq('source',src.name).eq('external_id',externalId).maybeSingle(); if(exists)continue;
   const row={source:src.name,external_id:externalId,link:x.link,kurum:institution(x.title,src.name),pozisyon:x.title.slice(0,250),aciklama:x.title,taban_puan:score(x.title),nitelik_kodlari:codes(x.title),sozlesme_turu:contract(x.title),published_at:new Date().toISOString()};
   const {data,error}=await supabase.from('announcements').insert(row).select().single(); if(!error&&data)inserted.push(data);
  }
  await supabase.from('last_sync').upsert({source:src.name,synced_at:new Date().toISOString(),item_count:candidates.length});
 }catch(e){console.error(src.name,e);}}
 try{await notifyNew(supabase,inserted);}catch(e){console.error('FCM error',e);}
 return new Response(JSON.stringify({ok:true,new_count:inserted.length,sources:SOURCES.map(x=>x.name)}),{headers:{...cors,'Content-Type':'application/json'}});
});

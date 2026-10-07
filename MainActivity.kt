package com.epilan.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.work.*
import java.util.concurrent.TimeUnit

 data class Announcement(val position:String,val institution:String,val source:String,val score:Int,val deadline:String,val code:String,val link:String="")

class MainActivity: ComponentActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
  scheduleSync(); setContent{ EpApp() }
 }
 private fun scheduleSync(){
  val req=PeriodicWorkRequestBuilder<SyncWorker>(30,TimeUnit.MINUTES).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build()
  WorkManager.getInstance(this).enqueueUniquePeriodicWork("ep-ilan-sync",ExistingPeriodicWorkPolicy.UPDATE,req)
 }
}

class SyncWorker(ctx:android.content.Context,params:WorkerParameters):CoroutineWorker(ctx,params){
 override suspend fun doWork():Result{return Result.success()}
}

@Composable fun EpApp(){
 var tab by remember{mutableIntStateOf(0)}
 var query by remember{mutableStateOf("")}
 var source by remember{mutableStateOf("Tümü")}
 val data=listOf(
  Announcement("Büro Personeli","Kamu Kurumu","Kariyer Kapısı",70,"24 Ekim 2026","3001"),
  Announcement("Memur","İstanbul Büyükşehir Belediyesi","Yerel Yönetimler",70,"30 Ekim 2026","3001"),
  Announcement("Sözleşmeli Personel","Devlet Üniversitesi","ilan.gov.tr",65,"29 Ekim 2026","3001"),
  Announcement("Zabıta Memuru","İlçe Belediyesi","Yerel Yönetimler",60,"2 Kasım 2026","3001")
 )
 Scaffold(bottomBar={NavigationBar{NavigationBarItem(selected=tab==0,onClick={tab=0},icon={Text("▣")},label={Text("İlanlar")});NavigationBarItem(selected=tab==1,onClick={tab=1},icon={Text("▤")},label={Text("CV")});NavigationBarItem(selected=tab==2,onClick={tab=2},icon={Text("◉")},label={Text("Profil")})}}){pad->
  Column(Modifier.padding(pad).padding(16.dp)){ when(tab){
   0->{Text("EP İlan",style=MaterialTheme.typography.headlineMedium);Text("KPSS kamu ilanlarını tek yerde takip et",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(12.dp));OutlinedTextField(value=query,onValueChange={query=it},modifier=Modifier.fillMaxWidth(),placeholder={Text("İlan, kurum veya kod ara")});Spacer(Modifier.height(8.dp));Button(onClick={},modifier=Modifier.fillMaxWidth()){Text("↻ 3 Kaynağı Senkronize Et")};Spacer(Modifier.height(8.dp));Row(horizontalArrangement=Arrangement.spacedBy(6.dp)){listOf("Tümü","Kariyer Kapısı","ilan.gov.tr","Yerel Yönetimler").forEach{s->FilterChip(selected=source==s,onClick={source=s},label={Text(s)})}};Spacer(Modifier.height(8.dp));LazyColumn{items(data.filter{(source=="Tümü"||it.source==source)&&("${it.position} ${it.institution} ${it.code}".contains(query,true))}){a->Card(Modifier.fillMaxWidth().padding(vertical=5.dp)){Column(Modifier.padding(15.dp)){Text(a.source,style=MaterialTheme.typography.labelSmall);Text(a.position,style=MaterialTheme.typography.titleMedium);Text(a.institution,color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(8.dp));Text("KPSS ${a.score} • Son: ${a.deadline} • ${a.code}")}}}}
   }
   1->{Text("CV'lerim",style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(15.dp));Card{Column(Modifier.padding(16.dp)){Text("KPSS Başvuru CV",style=MaterialTheme.typography.titleMedium);Text("Eğitim • İş deneyimi • Sertifikalar");Button(onClick={},modifier=Modifier.fillMaxWidth().padding(top=12.dp)){Text("+ Yeni CV")}}}
   2->{Text("Profilim",style=MaterialTheme.typography.headlineMedium);Spacer(Modifier.height(15.dp));Card{Column(Modifier.padding(16.dp)){Text("KPSS P3: 72",style=MaterialTheme.typography.titleMedium);Text("Nitelik kodu: 3001");Spacer(Modifier.height(15.dp));Text("Bildirimler",style=MaterialTheme.typography.titleMedium);Setting("Yeni ilan bildirimi",true);Setting("Puanım uygun ilanlar",true);Setting("Nitelik eşleşmesi",true);Setting("Sesli bildirim",true);Text("Otomatik tarama: 30 dakika")}}}
  }}
 }
}
@Composable fun Setting(t:String,on:Boolean){Row(Modifier.fillMaxWidth().padding(vertical=10.dp),horizontalArrangement=Arrangement.SpaceBetween){Text(t);Switch(checked=on,onCheckedChange={})}}

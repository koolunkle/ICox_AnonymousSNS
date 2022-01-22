package com.icox.anonymoussns

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ActionBar 의 타이틀을 "글목록"으로 변경
        supportActionBar?.title = "글목록"

        // floatingActionButton 이 클릭될 때의 Listener 를 설정한다
        floatingActionButton.setOnClickListener {
//            Intent 생성
            val intent = Intent(this@MainActivity, WriteActivity::class.java)
//            Intent 로 WriteActivity 실행
            startActivity(intent)
        }
    }

}
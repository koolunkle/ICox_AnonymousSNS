package com.icox.anonymoussns

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    // 로그에 사용할 문자열
    val TAG = "MainAtivity"

    // test 키를 가진 데이터의 참조 객체를 가져온다
    val ref = FirebaseDatabase.getInstance().getReference("test")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 값의 변경이 있는 경우의 이벤트 Listener 를 추가한다
        ref.addValueEventListener(object : ValueEventListener {
            // 데이터 변경이 감지되면 호출된다
            override fun onDataChange(snapshot: DataSnapshot) {
//                test 키를 가진 데이터 snapshot 에서 값을 읽고 문자열로 변경한다
                val message = snapshot.value.toString()
//                읽은 문자열 로깅
                Log.d(TAG, message)
//                Firebase 에서 전달받은 메세지로 제목을 변경한다
                supportActionBar?.title = message
            }

            // 데이터 읽기가 취소된 경우 호출된다 -> e.g. 데이터 권한이 없는 경우
            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }
        })
    }

}
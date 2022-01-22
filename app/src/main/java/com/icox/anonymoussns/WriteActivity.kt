package com.icox.anonymoussns

import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_write.*
import kotlinx.android.synthetic.main.card_background.view.*

class WriteActivity : AppCompatActivity() {

    /* res>drawable 디렉토리에 있는 배경 이미지를 uri 주소로 사용한다
    uri 주소로 사용하면 추후 웹에 있는 이미지 URL 도 바로 사용이 가능하다 */
    val backgroundList = mutableListOf(
        "android.resource://com.icox.anonymoussns/drawable/default_bg",
        "android.resource://com.icox.anonymoussns/drawable/bg2",
        "android.resource://com.icox.anonymoussns/drawable/bg3",
        "android.resource://com.icox.anonymoussns/drawable/bg4",
        "android.resource://com.icox.anonymoussns/drawable/bg5",
        "android.resource://com.icox.anonymoussns/drawable/bg6",
        "android.resource://com.icox.anonymoussns/drawable/bg7",
        "android.resource://com.icox.anonymoussns/drawable/bg8",
        "android.resource://com.icox.anonymoussns/drawable/bg9"
    )

    // 현재 선택된 배경 이미지의 position 을 저장하는 변수
    var currentBackgroundPosition = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_write)

        // ActionBar 의 타이틀을 "글쓰기"로 변경
        supportActionBar?.title = "글쓰기"

        // recyclerView 에서 사용할 layoutManager 를 생성한다
        val layoutManager = LinearLayoutManager(this@WriteActivity)

        // recyclerView 횡스크롤 설정
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL

        // recyclerView 에 layoutManager 를 방금 생성한 것으로 설정한다
        recyclerView.layoutManager = layoutManager

        // recyclerView 에 adapter 를 설정한다
        recyclerView.adapter = MyAdapter()

        // 공유하기 버튼이 클릭된 경우의 이벤트 Listener 를 설정한다
        sendButton.setOnClickListener {
//            메세지가 없는 경우 토스트 메세지로 알림
            if (TextUtils.isEmpty(input.text)) {
                Toast.makeText(applicationContext, "메시지를 입력하세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Post 객체 생성
            val post = Post()
            // Firebase 의 Posts 참조에서 객체를 저장하기 위한 새로운 key 를 생성하고 newRef 에 참조 저장
            val newRef = FirebaseDatabase.getInstance().getReference("Posts").push()

            // 글이 쓰여진 시간은 Firebase 서버 시간으로 설정
            post.writeTime = ServerValue.TIMESTAMP
            // 배경 Uri 주소를 현재 선택된 배경의 주소로 할당
            post.backgroundUri = backgroundList[currentBackgroundPosition]
            // 메세지는 input 의 텍스트 내용을 할당
            post.message = input.text.toString()
            // 글쓴 사람의 ID는 디바이스의 아이디로 할당
            post.writerId = getMyId()
            // 글의 ID는 새로 생성된 Firebase 참조의 key 로 할당
            post.postId = newRef.key.toString()
            // Post 객체를 새로 생성한 참조에 저장
            newRef.setValue(post)
            // 저장 성공 토스트 알림을 보여주고 Activity 종료
            Toast.makeText(applicationContext, "공유되었습니다", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // 디바이스의 ID를 반환하는 메소드 -> 글쓴 사람의 ID를 인식
    private fun getMyId(): String {
        return Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }

    // RecyclerView 에서 사용하는 ViewHolder 클래스
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.imageView
    }

    // RecyclerView 의 Adapter 클래스
    inner class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {
        // RecyclerView 에서 각 행에 그려낼 ViewHolder 를 생성할 때 불리는 메소드
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            // RecyclerView 에서 사용하는 ViewHolder 클래스를 card_background.xml 파일을 사용하도록 생성한다
            return MyViewHolder(
                LayoutInflater.from(this@WriteActivity)
                    .inflate(R.layout.card_background, parent, false)
            )
        }

        // 각 행의 position 에서 그려야 할 ViewHolder UI 에 데이터를 적용하는 메소드
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
//            피카소 객체로 ViewHolder 에 존재하는 imageView 에 이미지 로딩
            Picasso.get()
                .load(Uri.parse(backgroundList[position]))
                .fit()
                .centerCrop()
                .into(holder.imageView)

            // 각 배경화면 행이 클릭된 경우의 이벤트 Listener 설정
            holder.itemView.setOnClickListener {
                // 선택된 배경의 position 을 currentBackgroundPosition 에 저장
                currentBackgroundPosition = position
//                피카소 객체로 ViewHolder 에 존재하는 writeBackground 에 이미지 로딩
                Picasso.get().load(Uri.parse(backgroundList[position])).fit().centerCrop()
                    .into(writeBackground)
            }
        }

        // RecyclerView 에서 몇개의 행을 그리는지 기준이 되는 메소드
        override fun getItemCount(): Int {
            return backgroundList.size
        }
    }

}
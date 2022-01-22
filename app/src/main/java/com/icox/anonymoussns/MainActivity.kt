package com.icox.anonymoussns

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.card_background.view.*
import kotlinx.android.synthetic.main.card_background.view.imageView
import kotlinx.android.synthetic.main.card_post.view.*
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    val posts: MutableList<Post> = mutableListOf()

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

        // RecyclerView 에 LayoutManager 설정
        val layoutManager = LinearLayoutManager(this@MainActivity)

        // RecyclerView 의 아이템을 역순으로 정렬
        layoutManager.reverseLayout = true

        // RecyclerView 의 아이템을 쌓는 순서를 끝부터 쌓게 함
        layoutManager.stackFromEnd = true

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = MyAdapter()

        // Firebase 에서 Post 데이터를 가져온 후 posts 변수에 저장
        FirebaseDatabase.getInstance().getReference("/Posts").orderByChild("writeTime")
            .addChildEventListener(object : ChildEventListener {
                // 글이 추가된 경우
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot?.let { snapshot ->
                        // snapshot 의 데이터를 Post 객체로 가져옴
                        val post = snapshot.getValue(Post::class.java)
                        post?.let {
                            // 새 글이 마지막 부분에 추가된 경우
                            if (previousChildName == null) {
                                // 글 목록을 저장하는 변수에 Post 객체 추가
                                posts.add(it)
                                // recyclerView 의 adapter 에 글이 추가된 것을 알림
                                recyclerView.adapter?.notifyItemInserted(posts.size - 1)
                            } else {
                                // 글이 중간에 삽입된 경우 previousChildName 로 한단계 앞의 데이터 위치를 찾은 뒤 데이터를 추가한다
                                val prevIndex = posts.map { it.postId }.indexOf(previousChildName)
                                posts.add(prevIndex + 1, post)
                                // recyclerView 의 adapter 에 글이 추가된 것을 알림
                                recyclerView.adapter?.notifyItemInserted(prevIndex + 1)
                            }
                        }
                    }
                }

                // 글이 변경된 경우
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot?.let { snapshot ->
                        // snapshot 의 데이터를 Post 객체로 가져옴
                        val post = snapshot.getValue(Post::class.java)
                        post?.let { post ->
                            // 글이 변경된 경우 prevIndex 에 데이터를 변경한다
                            val prevIndex = posts.map { it.postId }.indexOf(previousChildName)
                            posts[prevIndex + 1] = post
                            recyclerView.adapter?.notifyItemChanged(prevIndex + 1)
                        }
                    }
                }

                // 글이 삭제된 경우
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    snapshot?.let {
                        // snapshot 의 데이터를 Post 객체로 가져옴
                        val post = snapshot.getValue(Post::class.java)
                        post?.let { post ->
                            // 기존에 저장된 인덱스를 찾아 해당 인덱스의 데이터를 삭제한다
                            val existIndex = posts.map { it.postId }.indexOf(post.postId)
                            posts.removeAt(existIndex)
                            recyclerView.adapter?.notifyItemRemoved(existIndex)
                        }
                    }
                }

                // 글의 순서가 이동한 경우
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot?.let {
                        // snapshot 의 데이터를 Post 객체로 가져옴
                        val post = snapshot.getValue(Post::class.java)
                        post?.let { post ->
                            // 기존의 인덱스를 구한다
                            val existIndex = posts?.map { it.postId }.indexOf(post.postId)
                            // 기존의 데이터를 지운다
                            posts.removeAt(existIndex)
                            recyclerView.adapter?.notifyItemRemoved(existIndex)
                            // previousChildName 가 없는 경우 -> 맨 마지막으로 이동
                            if (previousChildName == null) {
                                posts.add(post)
                                recyclerView.adapter?.notifyItemInserted(posts.size - 1)
                            } else {
                                // previousChildName 다음 글로 추가
                                val prevIndex = posts.map { it.postId }.indexOf(previousChildName)
                                posts.add(prevIndex + 1, post)
                                recyclerView.adapter?.notifyItemInserted(prevIndex + 1)
                            }
                        }
                    }
                }

                // 취소된 경우
                override fun onCancelled(error: DatabaseError) {
                    // 취소된 경우 에러 로그를 보여준다
                    error?.toException()?.printStackTrace()
                }
            })
    }

    // RecyclerView 에서 사용하는 ViewHolder 클래스
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // 글의 배경 ImageView
        val imageView: ImageView = itemView.imageView

        // 글의 내용 TextView
        val contentsText: TextView = itemView.contentsText

        // 글쓴 시간 TextView
        val timeTextView: TextView = itemView.timeTextView

        // 댓글 개수 TextView
        val commentCountText: TextView = itemView.commentCountText
    }

    // RecyclerView 의 Adapter 클래스
    inner class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {
        // RecyclerView 에서 각 행에 그려낼 ViewHolder 를 생성할 때 불리는 메소드
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(
                LayoutInflater.from(this@MainActivity).inflate(R.layout.card_post, parent, false)
            )
        }

        // 각 행의 position 에서 그려야 할 ViewHolder UI 에 데이터를 적용하는 메소드
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val post = posts[position]
            // 배경 이미지 설정
            Picasso.get().load(Uri.parse(post.backgroundUri)).fit().centerCrop()
                .into(holder.imageView)
            // 카드에 글을 세팅
            holder.contentsText.text = post.message
            // 글이 쓰여진 시간
            holder.timeTextView.text = getDiffTimeText(post.writeTime as Long)
            // 댓글 개수는 0으로 세팅
            holder.commentCountText.text = "0"
        }

        // RecyclerView 에서 몇개의 행을 그리는지 기준이 되는 메소드
        override fun getItemCount(): Int {
            return posts.size
        }
    }

    // 글이 쓰여진 시간을 "방금 전", "~시간 전", "~분 전", "yyyy년 MM월 dd일 HH:mm" 포맷으로 반환해주는 메소드
    private fun getDiffTimeText(targetTime: Long): String {
        val curDateTime = DateTime()
        val targetDateTime = DateTime().withMillis(targetTime)

        val diffDay = Days.daysBetween(curDateTime, targetDateTime).days
        val diffHours = Hours.hoursBetween(targetDateTime, curDateTime).hours
        val diffMinutes = Minutes.minutesBetween(targetDateTime, curDateTime).minutes

        if (diffDay == 0) {
            if (diffHours == 0 && diffMinutes == 0) {
                return "방금 전"
            }
            return if (diffHours > 0) {
                "${diffHours}시간 전"
            } else {
                "${diffMinutes}분 전"
            }
        } else {
            val format = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm")
            return format.format(Date(targetTime))
        }
    }

}
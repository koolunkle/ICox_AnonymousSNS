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
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    val commentList = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val postId = intent.getStringExtra("postId")

        val layoutManager = LinearLayoutManager(this@DetailActivity)
        layoutManager.orientation = LinearLayoutManager.HORIZONTAL
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = MyAdapter()

        // floatingActionButton 에 클릭 이벤트 Listener 설정
        floatingActionButton.setOnClickListener {
            // 글쓰기 화면으로 이동할 Intent 생성
            val intent = Intent(this@DetailActivity, WriteActivity::class.java)
            // 글쓰기 화면에서 댓글 쓰기인 것을 인식할 수 있도록 글쓰기 모드를 comment 로 전달
            intent.putExtra("mode", "comment")
            // 글의 ID를 전달
            intent.putExtra("postId", postId)
            // 글쓰기 화면 시작
            startActivity(intent)
        }

        // postID를 통해 Post 데이터로 다이렉트 접근
        FirebaseDatabase.getInstance().getReference("/Posts/$postId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot?.let {
                        val post = it.getValue(Post::class.java)
                        post?.let {
                            Picasso.get().load(it.backgroundUri)
                                .fit()
                                .centerCrop()
                                .into(backgroundImage)
                            contentsText.text = post.message
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        // postID를 통해 Comment 데이터를 가져온 후 commentList 에 저장
        FirebaseDatabase.getInstance().getReference("/Comments/$postId")
            .addChildEventListener(object : ChildEventListener {
                // 글이 추가된 경우
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot?.let { snapshot ->
                        // snapshot 의 데이터를 Comment 객체로 가져옴
                        val comment = snapshot.getValue(Comment::class.java)
                        comment?.let {
                            // 새 글이 마지막 부분에 추가된 경우
                            if (previousChildName == null) {
                                // 글 목록을 저장하는 변수에 Comment 객체 추가
                                commentList.add(it)
                                // recyclerView 의 adapter 에 글이 추가된 것을 알림
                                recyclerView.adapter?.notifyItemInserted(commentList.size - 1)
                            } else {
                                // 글이 중간에 삽입된 경우 previousChildName 으로 한단계 앞의 데이터 위치를 찾은 뒤 데이터 추가
                                val prevIndex =
                                    commentList.map { it.commentId }.indexOf(previousChildName)
                                commentList.add(prevIndex + 1, comment)
//                                recyclerView 의 adapter 에 글이 추가된 것을 알림
                                recyclerView.adapter?.notifyItemInserted(prevIndex + 1)
                            }
                        }
                    }
                }

                // 글의 순서가 이동한 경우
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                    snapshot?.let {
                        // snapshot 의 데이터를 Comment 객체로 가져옴
                        val comment = snapshot.getValue(Comment::class.java)
                        comment?.let { comment ->
                            // 기존의 인덱스를 구한다
                            val existIndex =
                                commentList.map { it.commentId }.indexOf(comment.commentId)
                            // 기존의 데이터를 지운다
                            commentList.removeAt(existIndex)
                            recyclerView.adapter?.notifyItemRemoved(existIndex)
                            // previousChildName 이 없는 경우 -> 맨 마지막으로 이동
                            if (previousChildName == null) {
                                commentList.add(comment)
                                recyclerView.adapter?.notifyItemInserted(commentList.size - 1)
                            } else {
                                // previousChildName 다음 글로 추가
                                val prevIndex =
                                    commentList.map { it.commentId }.indexOf(previousChildName)
                                commentList.add(prevIndex + 1, comment)
                                recyclerView.adapter?.notifyItemInserted(prevIndex + 1)
                            }
                        }
                    }
                }

                // 글이 삭제된 경우
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    snapshot?.let {
                        // snapshot 의 데이터를 Comment 객체로 가져옴
                        val comment = snapshot.getValue(Comment::class.java)
                        comment?.let { comment ->
                            // 기존에 저장된 인덱스를 찾아 해당 인덱스의 데이터를 삭제한다
                            val existIndex =
                                commentList.map { it.commentId }.indexOf(comment.commentId)
                            commentList.removeAt(existIndex)
                            recyclerView.adapter?.notifyItemRemoved(existIndex)
                        }
                    }
                }

                // 글이 변경된 경우
                override fun onChildChanged(
                    snapshot: DataSnapshot,
                    previousChildName: String?
                ) {
                    snapshot?.let { snapshot ->
                        // snapshot 의 데이터를 Comment 객체로 가져옴
                        val comment = snapshot.getValue(Comment::class.java)
                        comment?.let { comment ->
                            // 글이 변경된 경우 prevIndex 에 데이터를 변경한다
                            val prevIndex =
                                commentList.map { it.commentId }.indexOf(previousChildName)
                            commentList[prevIndex + 1] = comment
                            recyclerView.adapter?.notifyItemChanged(prevIndex + 1)
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
        val imageView = itemView.findViewById<ImageView>(R.id.background)
        val commentText = itemView.findViewById<TextView>(R.id.commentText)
    }

    // RecyclerView 의 Adapter 클래스
    inner class MyAdapter : RecyclerView.Adapter<MyViewHolder>() {
        // RecyclerView 에서 각 행에서 그려낼 ViewHolder 를 생성할 때 불리는 메소드
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            // RecyclerViewHolder 에서 사용하는 ViewHolder 클래스를 card_comment.xml 파일을 사용하도록 생성한다
            return MyViewHolder(
                LayoutInflater
                    .from(this@DetailActivity)
                    .inflate(R.layout.card_comment, parent, false)
            )
        }

        // 각 행의 position 에서 그려야 할 ViewHolder UI 에 데이터를 적용하는 메소드
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val comment = commentList[position]
            comment?.let {
                // 피카소 객체로 ViewHolder 에 존재하는 imageView 에 이미지 로딩
                Picasso.get()
                    .load(Uri.parse(comment.backgroundUri))
                    .fit()
                    .centerCrop()
                    .into(holder.imageView)
                holder.commentText.text = comment.message
            }
        }

        // RecyclerView 에서 몇개의 행을 그리는지 기준이 되는 메소드
        override fun getItemCount(): Int {
            return commentList.size
        }
    }

}
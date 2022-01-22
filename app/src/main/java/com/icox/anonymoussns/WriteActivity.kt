package com.icox.anonymoussns

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        }

        // RecyclerView 에서 몇개의 행을 그리는지 기준이 되는 메소드
        override fun getItemCount(): Int {
            return backgroundList.size
        }
    }

}

package com.azheng.scaleimage

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager

/**
 * @author azheng
 * @date 2019/12/30.
 * description：图片展示 缩放
 */
class ImageViewPagerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_view_pager)
        initData()
    }

    fun initData() {

        var imgUrlList = intent.getStringArrayListExtra("imgUrlList")?: mutableListOf<String>()
        var position = intent.getIntExtra("position", 0)

        var imageFragmentList: ArrayList<ImageViewPagerFragment> = ArrayList()

        imgUrlList.add("https://img.zcool.cn/community/02172958db679ca801219c7790da38.jpg")
        imgUrlList.add("http://img.mp.itc.cn/upload/20170809/031f78f109f84515a25f63e8caf3bb19_th.jpg")
        imgUrlList.add("http://img.mp.sohu.com/upload/20170515/2d9d1690c1324690a81c6cd968e13fdf_th.png")

        for (i in imgUrlList.indices) {
            val imageViewPagerFragment = ImageViewPagerFragment()
            val bundle = Bundle()
            bundle.putString("imgUrl", imgUrlList[i])
            bundle.putInt("position", i)
            bundle.putInt("imgUrlSize", imgUrlList.size)
            imageViewPagerFragment.arguments = bundle
            imageFragmentList.add(imageViewPagerFragment)
        }

        val viewPager = findViewById<ViewPager>(R.id.view_pager)
        viewPager.adapter = SamplePagerAdapter(supportFragmentManager, imageFragmentList)
        viewPager.currentItem = position

    }


    internal class SamplePagerAdapter(
        fm: FragmentManager,
        private var mListFragments: MutableList<ImageViewPagerFragment>
    ) :
        FragmentStatePagerAdapter(fm) {
        override fun getItem(position: Int): Fragment {
            return mListFragments[position]
        }

        override fun getCount(): Int {
            return mListFragments.size
        }


    }
}

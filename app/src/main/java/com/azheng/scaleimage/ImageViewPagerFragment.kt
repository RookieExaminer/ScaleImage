package com.azheng.scaleimage

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.PointF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

import com.bumptech.glide.request.target.BitmapImageViewTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.ImageViewState
import kotlinx.android.synthetic.main.fragment_image_view_pager.*

import java.io.File

/**
 * @author azheng
 * @date 2019/7/24.
 * description：展示图片
 */
class ImageViewPagerFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_image_view_pager, container, false)
    }


    private val MAX_SIZE = 4096
    private val MAX_SCALE = 8

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var imgUrl = arguments?.getString("imgUrl") ?: ""
        var position = arguments?.getInt("position")
        var imgUrlSize = arguments?.getInt("imgUrlSize")
        tvImageSizeView.setText("${(position ?: 0) + 1}/${imgUrlSize}")

        if (imgUrl != "") {
            if (ImageUtil.isGif(imgUrl)) {
                //加载动图
                Glide.with(this).load(imgUrl).into(photoViewImage)
            } else {
                downloadFile(imgUrl ?: "")
            }
        }

    }

    private fun downloadFile(imgUrl: String) {
        Glide.with(this).asBitmap().load(imgUrl)
            .into(object : BitmapImageViewTarget(photoViewImage) {

                @SuppressLint("CheckResult")
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    super.onResourceReady(resource, transition)
                    val h = resource.height
                    val w = resource.width

                    if (h >= MAX_SIZE || h / w > MAX_SCALE) {
                        photoViewImage.setVisibility(View.GONE)
                        photoViewSubsamplingScaleImage.setVisibility(View.VISIBLE)

                        Glide.with(this@ImageViewPagerFragment).load(imgUrl)
                            .downloadOnly(object : SimpleTarget<File>() {
                                override fun onResourceReady(
                                    resource: File,
                                    transition: Transition<in File>?
                                ) {
                                    val scale = ImageUtil.getImageScale(
                                        this@ImageViewPagerFragment.context!!,
                                        resource.absolutePath
                                    )
                                    photoViewSubsamplingScaleImage.setImage(
                                        ImageSource.uri(resource.absolutePath),
                                        ImageViewState(scale, PointF(0f, 0f), 0)
                                    )
                                }

                            })

                    } else {
                        photoViewImage.setImageBitmap(resource)
                    }
                }
            })

    }
}

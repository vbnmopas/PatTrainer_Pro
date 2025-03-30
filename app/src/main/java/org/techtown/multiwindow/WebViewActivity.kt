package org.techtown.multiwindow

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class WebViewActivity : AppCompatActivity() {

    // 웹뷰 선언
    lateinit var webView: WebView

    //카메라 변수 선언
    private lateinit var cameraExecutor: ExecutorService;
    private val CAMERA_PERMISSION_CODE = 100

    // 카메라 상태를 저장할 변수
    private var isFrontCamera = false

    //버튼 변수 선언
    lateinit var backButton : Button
    lateinit var btn3 : Button
    lateinit var btnSwitchCamera : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)


        backButton = findViewById<Button>(R.id.backButton)
        btn3 = findViewById<Button>(R.id.btn3)
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera)

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
//            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right) // 왼쪽에서 오른쪽으로 슬라이드 효과
            finish() // 현재 액티비티 종료
        }

        // 카메라 전환 버튼 클릭 리스너
        btnSwitchCamera.setOnClickListener {
            // 기존 카메라 상태 반전
            isFrontCamera = !isFrontCamera
            val previewView: PreviewView = findViewById(R.id.cameraView)
            checkAndRequestPermissions(previewView, isFrontCamera) // 카메라 전환
        }



        //카메라 실행
        val previewView: PreviewView = findViewById(R.id.cameraView)
        checkAndRequestPermissions(previewView, isFrontCamera) // 📌 권한 요청을 먼저 실행

        cameraExecutor = Executors.newSingleThreadExecutor()




        //웹뷰 시작 ------------------------------------------------------------------

        webView = findViewById(R.id.webView)

        webView.webViewClient = WebViewClient()
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings.domStorageEnabled = true
        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

//        Thread {
//            runOnUiThread {
//                webView.loadUrl("http://google.com")
//            }
//        }.start()


        webView.loadUrl("http://192.168.0.6:5000/video")

        //웹뷰 끝 -------------------------------------------------------------
    }



    //카메라 메서드
    private fun startCamera(previewView: PreviewView, isFrontCamera: Boolean) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = androidx.camera.core.Preview.Builder().build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            // 전면 카메라 선택 여부에 따른 카메라 설정
            val cameraSelector = if (isFrontCamera) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                val camera: Camera = cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
            } catch (exc: Exception) {
                Log.e("CameraX", "카메라 실행 실패", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    // 권한 체크 및 요청 메서드 (isFrontCamera 추가)
    private fun checkAndRequestPermissions(previewView: PreviewView, isFrontCamera: Boolean) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            startCamera(previewView, isFrontCamera) // 📌 권한이 있으면 카메라 실행 (isFrontCamera 추가)
        }
    }


}


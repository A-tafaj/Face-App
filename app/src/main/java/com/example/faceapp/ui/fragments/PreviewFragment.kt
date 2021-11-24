package com.example.faceapp.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import android.graphics.Camera
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraFilter
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraX
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.faceapp.R
import com.example.faceapp.databinding.FragmentPreviewBinding
import com.example.faceapp.utils.FaceApp
import com.example.faceapp.viewmodel.CameraViewModel
import com.google.common.util.concurrent.ListenableFuture

class PreviewFragment : Fragment() {
    private lateinit var binding: FragmentPreviewBinding
    private val CAMERA_CODE = 2
    lateinit var cameraSelector: CameraSelector
    private val cameraViewModel: CameraViewModel by viewModels()
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentPreviewBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        observeViewModel()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tryOpeningCamera()
        showSwitchCameraButton()
        listenSwitchCameraButton()
    }

    override fun onResume() {
        super.onResume()
        tryOpeningCamera()
    }

    private fun observeViewModel() {
        cameraViewModel.cameraSelector.observe(viewLifecycleOwner, {
            cameraSelector = it
        })
    }

    private fun tryOpeningCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireContext() as Activity, arrayOf(Manifest.permission.CAMERA),
                CAMERA_CODE
            )
        } else {
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        when (requestCode) {
            CAMERA_CODE -> {
                val permission = permissions[0]
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this.context, "Permission denied", Toast.LENGTH_LONG).show()
                    val showRationale = permission?.let { ActivityCompat.shouldShowRequestPermissionRationale(this.context as Activity, it) } ?: false
                    if (showRationale) {
                        Log.d(TAG, "onRequestPermissionsResult: access to camera is crucial to this app")
                    }
                } else {
                    startCamera()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startCamera() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
                Toast.makeText(requireContext(), getText(R.string.preview_failed), Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun showSwitchCameraButton() {
        if (!cameraViewModel.hasFrontCamera() || !cameraViewModel.hasBackCamera()) {
            binding.switchBtn.visibility = View.INVISIBLE
        }
    }

    private fun listenSwitchCameraButton() {
        binding.switchBtn.setOnClickListener {
            cameraViewModel.switchCameraSelector()
            startCamera()
        }
    }
}
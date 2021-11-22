package com.example.faceapp.ui.fragments

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.faceapp.adapters.EmotionListAdapter
import com.example.faceapp.databinding.FragmentImageBinding
import com.example.faceapp.utils.ImageInteractor
import com.example.faceapp.viewmodel.CameraViewModel

class ImageFragment : Fragment() {
    private val TAG = "CameraActivity"
    private val REQUEST_IMAGE_CAPTURE = 1
    private val CAMERA_CODE = 10
    private val REQUEST_OPEN_GALLERY = 2
    private val SELECT_PICTURE_CODE = 3
    private val imageInteractor = ImageInteractor()
    private val cameraViewModel: CameraViewModel by viewModels()

    private lateinit var binding: FragmentImageBinding
    private lateinit var emotionListAdapter: EmotionListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentImageBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        initializeRecyclerView()
        initClickListeners()
        observeViewModel()

        return binding.root
    }

    private fun observeViewModel() {
        cameraViewModel.imageIntent.observe(this, { intent ->
            try {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                Log.e(TAG, "dispatchTakePictureIntent: $e")
            }
        })

        cameraViewModel.passArrayOfEmotions.observe(viewLifecycleOwner, {
            emotionListAdapter.setMyListData(it)
        })
        cameraViewModel.passDrawnImage.observe(viewLifecycleOwner, { drawnImage ->
            binding.captureImg.setImageBitmap(drawnImage)
        })
        cameraViewModel.progressVisibility.observe(viewLifecycleOwner, {
            if (it) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.progressBar.visibility = View.INVISIBLE
            }
        })
    }

    fun initializeRecyclerView() {
        emotionListAdapter = EmotionListAdapter()
        binding.recyclerView.addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))

        with(binding.recyclerView) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(this.context)
            adapter = emotionListAdapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        emotionListAdapter.clear()
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            binding.captureImg.setImageBitmap(imageInteractor.getOrientatedImage(cameraViewModel.currentPhotoPath))
            cameraViewModel.detectAndFrame(cameraViewModel.currentPhotoPath)
        }

        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_OPEN_GALLERY) {
            // Get the url from data
            data?.data?.run {
                val path = imageInteractor.getPath(this)
                binding.captureImg.setImageBitmap(imageInteractor.getOrientatedImage(path))
                cameraViewModel.detectAndFrame(path)
            }
        }
    }

    private fun chooseImageFromGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), REQUEST_OPEN_GALLERY)
    }

    fun initClickListeners() {
        binding.captureBtn.setOnClickListener {
            openCamera()
        }
        binding.galleryBtn.setOnClickListener {
            openGallery()
        }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireContext() as Activity, arrayOf(Manifest.permission.CAMERA),
                CAMERA_CODE
            )
        } else {
            cameraViewModel.dispatchTakePictureIntent()
        }
    }

    private fun openGallery() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                requireContext() as Activity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                SELECT_PICTURE_CODE
            )
        } else {
            chooseImageFromGallery()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        when (requestCode) {
            SELECT_PICTURE_CODE -> {
                val permission = permissions[0]
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this.context, "Permission denied", Toast.LENGTH_LONG).show()
                    val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this.context as Activity, permission!!)
                    if (showRationale) {
                        Log.d(TAG, "onRequestPermissionsResult: access to media is crucial to this app")
                    }
                } else {
                    chooseImageFromGallery()
                }
            }
            CAMERA_CODE -> {
                val permission = permissions[0]
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this.context, "Permission denied", Toast.LENGTH_LONG).show()
                    val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this.context as Activity, permission!!)
                    if (showRationale) {
                        Log.d(TAG, "onRequestPermissionsResult: access to camera is crucial to this app")
                    }
                } else {
                    openCamera()
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
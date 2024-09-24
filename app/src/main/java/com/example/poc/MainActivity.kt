package com.example.poc

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {

    private lateinit var captureImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        textView = findViewById(R.id.textView)
        val captureButton: Button = findViewById(R.id.captureButton)

        // Verifica e solicita permissão de câmera
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        } else {
            // A permissão foi concedida, pode abrir a câmera
            captureButton.setOnClickListener {
                openCamera()
            }
        }

        captureImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageBitmap?.let {
                    // Processar a imagem capturada
                    processImage(it)
                }
            }
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        captureImageLauncher.launch(intent)
    }

    // Processar a imagem capturada
    private fun processImage(bitmap: Bitmap) {
        val inputImage: InputImage = InputImage.fromBitmap(bitmap, 0)

        // Inicializar o reconhecedor de texto
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        // Processar a imagem para reconhecimento de texto
        recognizer.process(inputImage)
            .addOnSuccessListener { visionText ->
                val recognizedText = visionText.text

                // Verificar se o valor da cédula foi reconhecido
                when {
                    recognizedText.contains("2") -> textView.text = "Cédula de 2 reais detectada"
                    recognizedText.contains("5") -> textView.text = "Cédula de 5 reais detectada"
                    recognizedText.contains("10") -> textView.text = "Cédula de 10 reais detectada"
                    recognizedText.contains("20") -> textView.text = "Cédula de 20 reais detectada"
                    recognizedText.contains("50") -> textView.text = "Cédula de 50 reais detectada"
                    recognizedText.contains("100") -> textView.text = "Cédula de 100 reais detectada"
                    else -> textView.text = "Cédula não reconhecida"
                }

                imageView.setImageBitmap(bitmap) // Exibir a imagem capturada
            }
            .addOnFailureListener { e ->
                textView.text = "Erro ao reconhecer texto: ${e.message}"
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissão concedida, pode abrir a câmera
                findViewById<Button>(R.id.captureButton).setOnClickListener {
                    openCamera()
                }
            } else {
                textView.text = "Permissão da câmera negada"
            }
        }
    }
}

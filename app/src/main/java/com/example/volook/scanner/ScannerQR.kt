package com.example.volook.scanner

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.volook.R
import com.example.volook.shared.api.ticket.TicketService
import com.example.volook.shared.services.ApiClient
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ScannerQR : AppCompatActivity() {

    private lateinit var barcodeView: BarcodeView
    private lateinit var imageView: ImageView
    private lateinit var qrStatusMessage: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scanner_qr)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scanner_qr)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        barcodeView = findViewById(R.id.barcode_view)
        imageView = findViewById(R.id.scanner_qr_image_view)
        qrStatusMessage = findViewById(R.id.scanner_qr_message)

        barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: com.journeyapps.barcodescanner.BarcodeResult?) {
                result?.let {
                    checkTicket(result.text)
7                }
            }
            override fun possibleResultPoints(resultPoints: List<com.google.zxing.ResultPoint>?) {}
        })
    }

    override fun onResume() {
        super.onResume()
        barcodeView.resume()
    }

    override fun onPause() {
        super.onPause()
        barcodeView.pause()
    }

    fun checkTicket(id:String){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val call = ApiClient.ticketService.findOne(id)
                call.enqueue(object : Callback<TicketService.Ticket> {
                    override fun onResponse(
                        call: Call<TicketService.Ticket>,
                        response: Response<TicketService.Ticket>
                    ) {
                        if(response.isSuccessful){
                            if(response.body()!=null){
                                val data = response.body() as TicketService.Ticket
                                Log.v("TICKET_FIND_ONE", data.toString())
                                if(data?.state == TicketService.TicketState.PURCHASED){
                                    changeAnimation(R.drawable.scanner_frame,R.drawable.scanner_frame_green,R.string.scanner_qr_valid )
                                }else{
                                    changeAnimation(R.drawable.scanner_frame,R.drawable.scanner_frame_red, R.string.scanner_qr_not_valid )
                                }
                            }
                        }else{
                            Log.v("TICKETS_FIND_ONE_ERROR", response.errorBody()!!.string())
                            changeAnimation(R.drawable.scanner_frame,R.drawable.scanner_frame_red, R.string.scanner_qr_not_valid )
                        }
                    }

                    override fun onFailure(
                        call: Call<TicketService.Ticket>,
                        t: Throwable
                    ) {
                        Log.v("TICKETS_FIND_ONE_FAILURE",t.toString())
                        changeAnimation(R.drawable.scanner_frame,R.drawable.scanner_frame_red, R.string.scanner_qr_not_valid )
                        //DISPLAY ERROR
                    }
                })
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("TICKETS_FIND_ONE_EXCEPTION ",e.toString())
                    changeAnimation(R.drawable.scanner_frame,R.drawable.scanner_frame_red, R.string.scanner_qr_not_valid )
                    //DISPLAY ERROR
                }
            }
        }
    }

    fun changeAnimation(oldDrawable: Int, newDrawable: Int, text: Int){
        imageView.setImageResource(newDrawable)
        qrStatusMessage.setText(text)
        imageView.animate()
            .setDuration(3000)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    imageView.setImageResource(oldDrawable)
                    qrStatusMessage.text = ""
                }
            })
            .start()
    }
}
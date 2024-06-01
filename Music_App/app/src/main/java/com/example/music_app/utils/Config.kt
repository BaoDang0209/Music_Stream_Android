package com.example.music_app.utils

import androidx.appcompat.app.AlertDialog
import android.content.Context
import com.example.music_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object Config {
    // Khai báo một biến dialog để lưu trữ AlertDialog hiện tại

    var dialog: AlertDialog? = null

    fun showDialog(context: Context) {
        // Tạo và hiển thị một AlertDialog với layout tùy chỉnh loading_layout
        // AlertDialog này không thể bị hủy bỏ bởi người dùng
        dialog = MaterialAlertDialogBuilder(context)
            .setView(R.layout.loading_layout)
            .setCancelable(false)
            .create()

        dialog!!.show()
    }

    fun hideDialog() {
        // Ẩn AlertDialog hiện tại
        dialog!!.dismiss()
    }
}
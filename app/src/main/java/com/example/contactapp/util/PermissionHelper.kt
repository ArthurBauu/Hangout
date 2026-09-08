package com.example.contactapp.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionHelper
{
    const val REQ_CODE_PERMS = 1001

    fun missingPermissions(activity: Activity): Array<String>
    {
        val perms = mutableListOf<String>()
        val needed = arrayOf(
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
        )
        for (p in needed)
        {
            if (ContextCompat.checkSelfPermission(activity, p) != PackageManager.PERMISSION_GRANTED)
            {
                perms.add(p)
            }
        }
        return perms.toTypedArray()
    }

    fun requestMissing(activity: Activity)
    {
        val miss = missingPermissions(activity)
        if (miss.isNotEmpty())
        {
            ActivityCompat.requestPermissions(activity, miss, REQ_CODE_PERMS)
        }
    }

    fun grantedResult(permissions: Array<out String>, grantResults: IntArray): Boolean
    {
        for (i in permissions.indices)
        {
            if (grantResults.getOrNull(i) != PackageManager.PERMISSION_GRANTED) return false
        }
        return true
    }
}

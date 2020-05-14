package com.soonsim.certviewer

import java.io.File
import java.io.FileInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class CertData() {
    var cn:String=""
    var ou:ArrayList<String> = ArrayList<String>()
    var o:String=""
    var c:String=""
    var stime:Long=0L
    var etime:Long=0L
    var isvalid:Boolean=false
    var path:String=""
    var certtext:String=""

    // file: directory containing SignCert.der, SignPri.key
    constructor(subjectdir: File) : this() {
        path=subjectdir.absolutePath
        parseSubjectLine(subjectdir.name)
        extractInfo(subjectdir)
    }

    fun getSummary() : String {
        var s=""
        s+="발행: " + ou.joinToString(" ") + "\n"
        s+="시작: " + SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(stime) + "\n"
        s+="종료: " + SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(etime)
        return s
    }

    private fun extractInfo(subjectdir:File) {
        val signcert=File(subjectdir, "SignCert.der")
        if (!signcert.isFile)
            return

        FileInputStream(signcert.absoluteFile).use { inStream ->
            val cf: CertificateFactory = CertificateFactory.getInstance("X.509")
            val cert: X509Certificate = cf.generateCertificate(inStream) as X509Certificate
            certtext=cert.toString()
            stime=cert.notBefore.toInstant().toEpochMilli()
            etime=cert.notAfter.toInstant().toEpochMilli()
            val now=Date().toInstant().toEpochMilli()
            isvalid=now in stime until etime
        }
    }

    fun parseSubjectLine(subjectline:String) {
        val tokens=subjectline.split(",")
        for (token in tokens) {
            val pair=token.split("=")
            when (pair[0]) {
                "cn" -> { cn = pair[1] }
                "ou" -> { ou.add(pair[1]) }
                "o" -> { o = pair[1] }
                "c" -> { c = pair[1] }
            }
        }
    }
}
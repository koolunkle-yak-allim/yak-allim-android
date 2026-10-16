package com.example.yakallim.domain.model

data class Prescription(
    val medicines: List<PrescribedMedicine> = emptyList()
)

package io.github.koolunkle.yakallim.domain.model

data class Prescription(
    val medicines: List<PrescribedMedicine> = emptyList()
)

package io.github.koolunkle.yakallim.domain.model

data class Alarm(
    val times: List<String>,
    val soundUri: String?
)

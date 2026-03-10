package org.yankap.yankap.domain.models

enum class Operator(val displayName: String, val ussdPrefix: String) {
    MTN("MTN Mobile Money", "*126#"),
    ORANGE("Orange Money", "*150#"),
    CAMTEL("Camtel Money", "*160#"),
    NEXTTEL("Nexttel Possa", "*282#")
}

package dev.dzgeorgy.kameleon.sample

data class Track(
    val name: String,
    val length: Float
)

data class TrackEntity(
    val name: String,
    val length: Float
)

fun main() {
    val track = Track("Blank Space", 3.51f)
    val trackEntity = TrackEntity("Blank Space", 3.51f)
    println(track)
    println(trackEntity)
}

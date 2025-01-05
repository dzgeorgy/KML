package dev.dzgeorgy.kameleon.sample

import dev.dzgeorgy.kameleon.lib.MapTo

@MapTo(TrackEntity::class)
@MapTo(TrackModel::class)
data class Track(
    val name: String,
    val length: Float,
    val author: String
)

data class TrackEntity(
    val name: String,
    val length: Float
)

@MapTo(TrackEntity::class)
data class TrackModel(
    val name: String,
    val length: Float
)

fun main() {
    // Define objects.
    val track = Track("Blank Space", 3.51f, "Taylor Swift")
    val trackEntity = TrackEntity("Blank Space", 3.51f)
    val trackModel = TrackModel("Blank Space", 3.51f)

    // Print objects data.
    println(track)
    println(trackEntity)
    println(trackModel)

    // Assert objects data is equal.
    println(track.toTrackEntity() == trackEntity)
    println(track.toTrackModel() == trackModel)
    println(trackModel.toTrackEntity() == trackEntity)
}

package dev.dzgeorgy.kameleon.sample

import dev.dzgeorgy.kameleon.lib.Alias
import dev.dzgeorgy.kameleon.lib.MapTo

@MapTo(TrackEntity::class)
@MapTo(TrackModel::class)
data class Track(
    @property:Alias("_name")
    val name: String,
    @property:Alias("_length")
    val length: Float
)

@MapTo(Track::class)
class TrackEntity(
    @Alias("name")
    _name: CharSequence,
    _length: Float
) {
    val name: String = _name.toString()
    val length: Float = _length

    override fun toString(): String {
        return "TrackEntity(name=$name, length=$length)"
    }
}

@MapTo(TrackEntity::class)
class TrackModel(
    @property:Alias("name")
    val trackName: String,
    @property:Alias("_length")
    val length: Float
) {
    override fun toString(): String {
        return "TrackModel(trackName=$trackName, length=$length)"
    }
}

fun main() {
    val track = Track("Blank Space", 3.51f)
    val trackEntity = TrackEntity("Blank Space", 3.51f)
    val trackModel = TrackModel("Blank Space", 3.51f)
    println("TRACK: $track")
    println("TRACK_TO_ENTITY: ${track.toTrackEntity()}")
    println("TRACK_TO_MODEL: ${track.toTrackModel()}")
    println("ENTITY: $trackEntity")
    println("ENTITY_TO_TRACK: ${trackEntity.toTrack()}")
    println("MODEL: $trackModel")
    println("MODEL_TO_ENTITY: ${trackModel.toTrackEntity()}")
}

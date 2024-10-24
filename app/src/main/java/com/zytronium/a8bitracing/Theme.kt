package com.zytronium.a8bitracing

data class Theme(
	val id: Int,
	val name: String,
	val backgroundTexture: Int,
	val backgroundTextureBlurred: Int,
	val blueVehicleTexture: Int,
	val redVehicleTexture: Int,
	val greenVehicleTexture: Int
)

val raceTrack = Theme(
	0,
	"Race Track",
	R.drawable.race_road,
	R.drawable.race_road_blur,
	R.drawable.blue_car,
	R.drawable.red_car,
	R.drawable.green_car
	)

val spaceRace = Theme(
	1,
	"Space Race",
	R.drawable.race_space,
	R.drawable.race_space_blur,
	R.drawable.blue_raceship,
	R.drawable.red_raceship,
	R.drawable.green_raceship
)

val subspaceRift = Theme(
	2,
	"Subspace Rift",
	R.drawable.subspace_rift_background_mk4,
	R.drawable.subspace_rift_background_mk4_blur,
	R.drawable.blue_rift_racer,
	R.drawable.red_rift_racer,
	R.drawable.green_rift_racer
)

val glitch = Theme(
	3,
	"Glitched",
	R.drawable.glitch_race,
	R.drawable.glitch_race_crystal_blur,
	R.drawable.blue_glitched_racer,
	R.drawable.red_glitched_racer,
	R.drawable.green_glitched_racer
)

val rainbowKart = Theme(
	4,
	"Rainbow Kart",
	R.drawable.rainbow_road,
	R.drawable.rainbow_road_blur,
	R.drawable.blue_car,
	R.drawable.red_car,
	R.drawable.green_car
)

object Themes {
	val themes = mapOf("Race Track" to raceTrack, "Space Race" to spaceRace, "Subspace Rift" to subspaceRift, "Glitched" to glitch, "Rainbow Kart" to rainbowKart)
}
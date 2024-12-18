package com.example.testmaplibre

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.testmaplibre.databinding.ActivityMainBinding
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.FillExtrusionLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.sources.GeoJsonSource
import java.io.IOException
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
	private lateinit var binding: ActivityMainBinding
	private lateinit var mapLibreMap: MapLibreMap

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		MapLibre.getInstance(this)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		binding.mapView.onCreate(savedInstanceState)
		settingMapLibre()

	}

	private fun settingMapLibre() {
		binding.mapView.getMapAsync { map ->
			mapLibreMap = map
			// 2d bright // 3d liberty
			map.setStyle("https://tiles.openfreemap.org/styles/liberty") { style ->
				// show 3d building from asset geojson
				add3DLayer(style)

				// show line from asset geojson
				addLineSource(style)
			}
			map.cameraPosition =
				CameraPosition.Builder()
					.target(LatLng(35.71509523455584, 139.8498240030706))
					.zoom(15.0).build()
		}
	}

	private fun addLineSource(style: Style) {
		try {
			// Read GeoJSON from assets
			val geoJsonString = loadGeoJsonFromAsset("tran.geojson")

			// Create GeoJSON source
			val geoJsonSource = GeoJsonSource("route-source", geoJsonString)
			style.addSource(geoJsonSource)

			// Create a line layer to render the LineString
			val lineLayer = LineLayer("route-layer", "route-source")

			// Style the line with more explicit properties
			lineLayer.setProperties(
				// Ensure line is visible
				PropertyFactory.lineColor(android.graphics.Color.RED),
				PropertyFactory.lineWidth(5f),
				PropertyFactory.lineOpacity(1f),
				PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
				PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND)
			)

			// Add the layer to the map
			style.addLayer(lineLayer)

		} catch (e: Exception) {
			Log.e("GeoJSON", "Error loading GeoJSON", e)
		}
	}

	private fun add3DLayer(style: Style) {
		try {
			// Load GeoJSON from the assets folder
			val geoJson = loadGeoJsonFromAsset("sample.geojson")

			// Add GeoJsonSource
			val geoJsonSource = GeoJsonSource("geojson-source", geoJson)
			style.addSource(geoJsonSource)

			val altitudes = parseAltitudeFromGeoJson(geoJson)
			// Add FillExtrusionLayer
			val lineExtrusionLayer =
				FillExtrusionLayer("line-extrusion-layer", "geojson-source").apply {
					setProperties(
						PropertyFactory.fillExtrusionColor(Color.BLUE), // Line color (extrusion color)
						PropertyFactory.fillExtrusionHeight(
							altitudes.average().toFloat()
						), // Set height (altitude)
						PropertyFactory.fillExtrusionBase(0.0f), // Base height (floor level)
						PropertyFactory.fillExtrusionOpacity(0.7f),
					)
				}

			// Add layer to the map
			style.addLayer(lineExtrusionLayer)

		} catch (e: Exception) {
			Log.e("3D Layer", "Error adding 3D layer", e)
		}
	}

	private fun parseAltitudeFromGeoJson(geoJsonString: String): List<Float> {
		val altitudes = mutableListOf<Float>()

		// Parse the GeoJSON
		val jsonObject = JSONObject(geoJsonString)
		val features = jsonObject.getJSONArray("features")

		for (i in 0 until features.length()) {
			val feature = features.getJSONObject(i)
			val geometry = feature.getJSONObject("geometry")

			// Check if coordinates exist and extract altitude
			if (geometry.has("coordinates")) {
				val coordinates = geometry.getJSONArray("coordinates")
				// Extract altitude (third element) from each coordinate
				for (j in 0 until coordinates.length()) {
					val coordinate = coordinates.getJSONArray(j)
					val altitude = coordinate.optDouble(2, 0.0) // Default to 0.0 if no altitude
					altitudes.add(altitude.toFloat())
				}
			}
		}

		return altitudes
	}

	private fun loadGeoJsonFromAsset(fileName: String): String {
		return try {
			val inputStream = assets.open(fileName)
			val size = inputStream.available()
			val buffer = ByteArray(size)
			inputStream.read(buffer)
			inputStream.close()
			String(buffer, Charset.forName("UTF-8"))
		} catch (e: IOException) {
			e.printStackTrace()
			""
		}
	}

	override fun onStart() {
		super.onStart()
		binding.mapView.onStart()
	}

	override fun onResume() {
		super.onResume()
		binding.mapView.onResume()
	}

	override fun onPause() {
		super.onPause()
		binding.mapView.onPause()
	}

	override fun onStop() {
		super.onStop()
		binding.mapView.onStop()
	}

	override fun onLowMemory() {
		super.onLowMemory()
		binding.mapView.onLowMemory()
	}

	override fun onDestroy() {
		super.onDestroy()
		binding.mapView.onDestroy()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		binding.mapView.onSaveInstanceState(outState)
	}
}
package com.example.graphchartslib

import android.graphics.Color
import android.graphics.DashPathEffect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.example.graphchartslib.databinding.ActivityMainBinding
import com.google.android.material.color.MaterialColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
    }


private fun handleError(message: String?) {
    binding.progressBar.hide()
}

private fun handleEvScoreSuccess(data: EvScoreDomain?) {
    binding.progressBar.hide()

    if (data == null)
        return

    data.efficiencyScore?.let { binding.evScore.text = String.format("%.2f", it) }
    data.chargingScore?.let { binding.charPerfValue.text = String.format("%.2f", it) }
    data.drivingScore?.let {
        binding.drivBehValue.text = String.format("%.2f", it)
        binding.value.text = String.format("%.2f", it)
    }
    data.sustainabilityScore?.let { binding.sustMobValue.text = String.format("%.2f", it) }
    setRadarData(
        (data.sustainabilityScore ?: 0.0),
        (data.chargingScore ?: 0.0),
        (data.drivingScore ?: 0.0)
    )

    data.drivingBehavior?.let { db ->
        db.overspeeding?.let { binding.overspeedValue.text = String.format("%.2f", it) }
        db.braking?.let { binding.breakingValue.text = String.format("%.2f", it) }
        db.acceleration?.let { binding.accValue.text = String.format("%.2f", it) }
        db.regen?.let { binding.regenValue.text = String.format("%.2f", it) }

        guageChart(
            db.overspeeding ?: 0.0,
            db.braking ?: 0.0,
            db.acceleration ?: 0.0,
            db.regen ?: 0.0
        )

    }


    data.chargingPerfomance?.let { cp ->
        cp.overnightCharging?.let { binding.overnightPoint.text = String.format("%.2f", it) }
        cp.healthyCharging?.let { binding.healthyPoint.text = String.format("%.2f", it) }
        cp.fullChargingCompilence?.let { binding.compliancePoint.text = String.format("%.2f", it) }

        cp.overnightCharging?.let {
            binding.overnightProgressBar.progress = (it * 10.0 + 1).toInt()
        } // done for a reason as android has some serious bug where multiple progress bar behave weirdly when updating.
        cp.overnightCharging?.let { binding.overnightProgressBar.progress = (it * 10.0).toInt() }
        cp.healthyCharging?.let {
            binding.healthyProgressBar.progress = (it * 10.0 + 1).toInt()
        }  // done for a reason as android has some serious bug where multiple progress bar behave weirdly when updating.
        cp.healthyCharging?.let { binding.healthyProgressBar.progress = (it * 10.0).toInt() }
        cp.fullChargingCompilence?.let {
            binding.complianceProgressBar.progress =
                (it * 10.0 + 1).toInt() // done for a reason as android has some serious bug where multiple progress bar behave weirdly when updating.
        }

        cp.fullChargingCompilence?.let {
            binding.complianceProgressBar.progress = (it * 10.0).toInt()
        }

        setPieData(cp.fastChargingScore ?: 0.0, cp.normalChargingScore ?: 0.0)

    }

    data.sustainabilityMobility?.let {
        setLineData(it)
    }

}

override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View? {
    // Inflate the layout for this fragment
    binding = FragmentEvscoreBinding.inflate(layoutInflater, container, false)
    return binding.root
}

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setClickListener()
    pieChart()
    lineChart()
    // guageChart()
    radarChart()
}

private fun radarChart() {
    setRadarChart()
    setRadarXAxis()
    setRadarYAxis()
    setRadarLegend()
    //setRadarData()
}

private fun setRadarData(param: Double, param1: Double, param2: Double) {

    val entries2 = java.util.ArrayList<RadarEntry>()

    entries2.add(
        RadarEntry(
            (param * 10).toFloat(),
            requireActivity().getDrawable(R.drawable.green_marker)
        )
    )
    entries2.add(
        RadarEntry(
            (param1 * 10).toFloat(),
            requireActivity().getDrawable(R.drawable.yellow_marker)
        )
    )
    entries2.add(
        RadarEntry(
            (param2 * 10).toFloat(),
            requireActivity().getDrawable(R.drawable.blue_marker)
        )
    )
    /*
            entries2.add(RadarEntry(92.8f, requireActivity().getDrawable(R.drawable.green_marker)))
            entries2.add(RadarEntry(78.1f, requireActivity().getDrawable(R.drawable.yellow_marker)))
            entries2.add(RadarEntry(60.3f, requireActivity().getDrawable(R.drawable.blue_marker)))*/

    val set2 = RadarDataSet(entries2, "This Week")
    set2.color = Color.rgb(255, 0, 0)
    set2.fillAlpha = 180
    set2.lineWidth = 2f
    set2.isDrawHighlightCircleEnabled = false
    set2.setDrawHighlightIndicators(false)
    set2.highlightCircleStrokeColor = Color.GREEN


    set2.highLightColor = Color.rgb(255, 0, 0)
    set2.setDrawIcons(true)
    set2.setDrawValues(true)

    val sets = java.util.ArrayList<IRadarDataSet>()
    // sets.add(set1);
    // sets.add(set1);
    sets.add(set2)

    val data = RadarData(sets)
    //data.setValueTypeface(tfLight)
    data.setValueTextSize(8f)
    data.setDrawValues(false)
    data.setValueTextColor(Color.RED)

    data.isHighlightEnabled = true

    binding.radarChart.setData(data)
    binding.radarChart.invalidate()
}

private fun setRadarLegend() {
    binding.radarChart.legend.isEnabled = false
}

private fun setRadarYAxis() {
    val yAxis: YAxis = binding.radarChart.getYAxis()
    //yAxis.typeface = tfLight
    yAxis.setLabelCount(5, true)
    yAxis.textSize = 9f
    yAxis.axisMinimum = 0f
    yAxis.axisMaximum = 100f
    yAxis.setDrawLabels(true)
    context?.let {
        yAxis.textColor = ContextCompat.getColor(it, R.color.color_secondary_87)
    }

    yAxis.valueFormatter =
        IAxisValueFormatter { value, axis -> (value / 10).toInt().toString() + "" }

}

private fun setRadarXAxis() {
    val xAxis: XAxis = binding.radarChart.getXAxis()
    //xAxis.typeface = tfLight
    xAxis.textSize = 9f
    xAxis.yOffset = 0f
    xAxis.xOffset = 0f
    xAxis.valueFormatter = object : IAxisValueFormatter {
        // text in below arrays are irrelevent for now, but dont remove  as it will increase the size and thats need to be handled inside library.  use it whenever required.
        private val mActivities = arrayOf("B", "S", "S")
        override fun getFormattedValue(value: Float, axis: AxisBase): String {
            return mActivities[value.toInt() % mActivities.size]
        }
    }
    xAxis.textColor = Color.TRANSPARENT
}

private fun setRadarChart() {
    binding.radarChart.setBackgroundColor(
        MaterialColors.getColor(
            binding.radarChart,
            R.attr.colorSurface
        )
    )

    binding.radarChart.getDescription().setEnabled(false)

    binding.radarChart.setWebLineWidth(1f)
    binding.radarChart.setWebColor(Color.LTGRAY)
    binding.radarChart.setWebLineWidthInner(1f)
    binding.radarChart.setWebColorInner(Color.LTGRAY)
    binding.radarChart.setWebAlpha(100)
    binding.radarChart.setRotationEnabled(false)

    // create a custom MarkerView (extend MarkerView) and specify the layout
    // to use for it

    // create a custom MarkerView (extend MarkerView) and specify the layout
    // to use for it
    val mv: MarkerView = RadarMarkerView(requireContext(), R.layout.radar_marker_view)
    mv.chartView = binding.radarChart // For bounds control

    binding.radarChart.setMarker(mv) // Set the marker to the chart

    binding.radarChart.animateXY(1400, 1400, Easing.EaseInOutQuad)

}

private fun guageChart(overspeed: Double, braking: Double, acc_Value: Double, regen: Double) {
    Log.d("height", binding.multiGauge.measuredHeight.toString())
    val range = Range()
    range.color = Color.parseColor("#00527D")
    range.from = 0.0
    range.to = 10.0

    val range2 = Range()
    range2.color = Color.parseColor("#0077A5")
    range2.from = 0.0
    range2.to = 10.0

    val range3 = Range()
    range3.color = Color.parseColor("#179FCF")
    range3.from = 0.0
    range3.to = 10.0

    val range4 = Range()
    range4.color = Color.parseColor("#5AC8FA")
    range4.from = 0.0
    range4.to = 10.0

    binding.multiGauge.minValue = 0.0
    binding.multiGauge.maxValue = 10.0
    binding.multiGauge.value = overspeed

    binding.multiGauge.secondMinValue = 0.0
    binding.multiGauge.secondMaxValue = 10.0
    binding.multiGauge.secondValue = braking

    binding.multiGauge.thirdMinValue = 0.0
    binding.multiGauge.thirdMaxValue = 10.0
    binding.multiGauge.thirdValue = acc_Value

    binding.multiGauge.fourthMinValue = 0.0
    binding.multiGauge.fourthMaxValue = 10.0
    binding.multiGauge.fourthValue = abs(regen)


    binding.multiGauge.isDisplayValuePoint = true
    binding.multiGauge.isUseRangeBGColor = true

    binding.multiGauge.addRange(range)
    binding.multiGauge.addSecondRange(range2)
    binding.multiGauge.addThirdRange(range3)
    binding.multiGauge.addFourthRange(range4)


}

private fun setClickListener() {
    binding.ivBackEvscore.setOnClickListener(this)
}

private fun pieChart() {
    setPieChart()
    setPieLegend()
    //setPieData()
}

private fun lineChart() {
    setLineChart()
    setLineChartXAxis()
    setLineChartYAxis()
    //setLineData(null)
    setLineLegend()

}

private fun setLineLegend() {
    binding.linechart.legend.apply {
        isEnabled = false
    }
}

private fun setLineChartYAxis() {
    // // Y-Axis Style // //
    binding.linechart.axisRight.isEnabled = false
    binding.linechart.axisLeft.apply {
        setDrawGridLines(false)
        setLabelCount(6)
        //setAxisMaximum(100f)
        setAxisMinimum(0f)
        setTextColor(MaterialColors.getColor(binding.linechart, R.attr.colorSecondary))
        setDrawGridLines(true)
        setDrawGridLinesBehindData(true)
        setGridColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.primary_color_blue_opacity6
            )
        );
    }
}

private fun setLineChartXAxis() {

    binding.linechart.xAxis.apply {
        setDrawGridLines(false)
        position = XAxis.XAxisPosition.BOTTOM
        granularity = 1f
        axisMinimum = 1f
        axisMaximum = 31.2f
        labelCount = 7
        setAvoidFirstLastClipping(true)
        gridColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)
        textColor = MaterialColors.getColor(binding.linechart, R.attr.colorSecondary)
    }

}

private fun setLineChart() {
    // background color

    // background color

    binding.linechart.apply {
        setBackgroundColor(Color.WHITE)
        description.isEnabled = false
        setTouchEnabled(true)
        setDrawGridBackground(false)
        val mv = MyMarkerView(requireContext(), R.layout.custom_marker_view)
        // Set the marker to the chart
        // Set the marker to the chart
        mv.setChartView(binding.linechart)
        setMarker(mv)
        // enable scaling and dragging
        // enable scaling and dragging
        setDragEnabled(true)
        setScaleEnabled(true)
        // chart.setScaleXEnabled(true);
        // chart.setScaleYEnabled(true);

        // force pinch zoom along both axis
        // chart.setScaleXEnabled(true);
        // chart.setScaleYEnabled(true);

        // force pinch zoom along both axis
        setPinchZoom(false)
        setDrawGridBackground(false)
        // chart.highisHighlightFullBarEnabled = false
        // chart.highisHighlightFullBarEnabled = false
        setVisibleXRangeMaximum(10f)
        setPinchZoom(false)
        setScaleEnabled(false)
        setExtraBottomOffset(10f)
    }
}

val sdf = SimpleDateFormat("yyyy-MM-dd", Locale("en"))
private val dateformat = SimpleDateFormat("dd")
private fun setLineData(list: List<EvScoreDomain.SustainabilityMobilityDomain?>) {

    if (list.isEmpty())
        return

    val startDate = list[list.size - 1]!!.date
    val endDate = list[0]!!.date

    if (dateformat.format(sdf.parse(startDate)) >= dateformat.format(sdf.parse(endDate))) {
        // show two months
        // if both year same
        if (SimpleDateFormat("yyyy").format(sdf.parse(startDate)) == SimpleDateFormat("yyyy").format(
                sdf.parse(endDate)
            )
        ) {

            if (SimpleDateFormat("MMMM").format(sdf.parse(startDate))
                    .uppercase() == SimpleDateFormat("MMMM").format(sdf.parse(endDate))
                    .uppercase()
            ) {
                binding.chartMonthEvscore.text =
                    SimpleDateFormat("MMMM").format(sdf.parse(startDate))
                        .uppercase() + " " + SimpleDateFormat("yyyy").format(sdf.parse(startDate))
            } else
                binding.chartMonthEvscore.text =
                    SimpleDateFormat("MMMM").format(sdf.parse(startDate))
                        .uppercase() + "/" + SimpleDateFormat("MMMM").format(sdf.parse(endDate))
                        .uppercase() + " " + SimpleDateFormat("yyyy").format(sdf.parse(startDate))
        } else
            binding.chartMonthEvscore.text = SimpleDateFormat("MMMM").format(sdf.parse(startDate))
                .uppercase() + " " + SimpleDateFormat("yyyy").format(sdf.parse(startDate)) + "/" + SimpleDateFormat(
                "MMMM"
            ).format(sdf.parse(endDate))
                .uppercase() + " " + SimpleDateFormat("yyyy").format(sdf.parse(endDate))
    } else {
        //show single month
        binding.chartMonthEvscore.text = SimpleDateFormat("MMMM").format(sdf.parse(startDate))
            .uppercase() + " " + SimpleDateFormat("yyyy").format(sdf.parse(startDate))
    }
    binding.linechart.xAxis.mAxisMaximum = (list.size + 1).toFloat()
    binding.linechart.xAxis.valueFormatter =
        IndexAxisValueFormatter(getMonthlyDates(list.size + 1, endDate))
    //linechart.setVisibleXRangeMinimum(10f)

    val values = java.util.ArrayList<Entry>()

    var counter = 1
    for (item in list.asReversed()) {
        values.add(Entry(counter++.toFloat(), item!!.kmDriven!!.toFloat()))
    }
    /* for(i in 0..10)
        values.add(Entry(counter++.toFloat(), 0f))*/

    /*  values.add(Entry(1f, 20f))
      values.add(Entry(2f, 60f))
      values.add(Entry(3f, 10f))
      values.add(Entry(29f, 20f))
      values.add(Entry(30f, 60f))
*/
    val set1: LineDataSet
    if (binding.linechart.getData() != null &&
        binding.linechart.data.dataSetCount > 0
    ) {
        set1 = binding.linechart.data.getDataSetByIndex(0) as LineDataSet
        set1.values = values
        set1.notifyDataSetChanged()
        binding.linechart.getData().notifyDataChanged()
        binding.linechart.notifyDataSetChanged()
    } else {
        // create a dataset and give it a type
        set1 = LineDataSet(values, "DataSet 1")
        set1.setDrawIcons(false)

        // draw dashed line
        set1.enableDashedLine(10f, 5f, 0f)

        // black lines and points
        set1.color = -0x882670
        set1.setCircleColor(-0x882670)

        // line thickness and point size
        set1.lineWidth = 1f
        set1.circleRadius = 3f

        // draw points as solid circles
        set1.setDrawCircleHole(false)

        // customize legend entry
        set1.formLineWidth = 1f
        set1.formLineDashEffect = DashPathEffect(floatArrayOf(10f, 5f), 0f)
        set1.formSize = 15f

        // text size of values
        set1.valueTextSize = 9f

        // draw selection line as dashed
        set1.disableDashedLine()
        //set1.enableDashedHighlightLine(10f, 5f, 0f);

        // set the filled area
        set1.setDrawFilled(true)
        set1.fillFormatter =
            IFillFormatter { dataSet, dataProvider ->
                binding.linechart.getAxisLeft().getAxisMinimum()
            }
        set1.setDrawValues(false)

        // set color of filled area
        if (Utils.getSDKInt() >= 18) {
            // drawables only supported on api level 18 and above
            val drawable = ContextCompat.getDrawable(requireContext(), R.drawable.fade_green)
            set1.fillDrawable = drawable
        } else {
            set1.fillColor = Color.BLACK
        }
        val dataSets = java.util.ArrayList<ILineDataSet>()
        dataSets.add(set1) // add the data sets

        // create a data object with the data sets
        val data = LineData(dataSets)

        // set data
        when {
            values.size < 10 -> binding.linechart.setScaleMinima(1f, 1f)
            values.size in 10..20 -> binding.linechart.setScaleMinima(2f, 1f)
            values.size in 20..25 -> binding.linechart.setScaleMinima(3f, 1f)
            else -> binding.linechart.setScaleMinima(4f, 1f)
        }

        //  linechart.setScaleMinima(4f, 1f)
        binding.linechart.centerViewToAnimated(
            binding.linechart.getXAxis().getAxisMaximum(),
            0f,
            YAxis.AxisDependency.LEFT,
            200
        )
        binding.linechart.data = data
    }
}

private lateinit var monthlyarr: Array<String?>
private lateinit var returnToDisplayInXxis: Array<String?>
private val datemonthformat = SimpleDateFormat("ddMMM")
private fun getMonthlyDates(size: Int, endDate: String?): Array<String?> {
    monthlyarr = arrayOfNulls<String>(size)
    returnToDisplayInXxis = arrayOfNulls(size)
    monthlyarr[0] = ""
    returnToDisplayInXxis[0] = ""
    var cal = Calendar.getInstance()
    cal.time = sdf.parse(endDate)
    for (i in 1 until size) {
        cal.add(Calendar.DATE, ((size - 1) - i) * -1)
        monthlyarr[i] = datemonthformat.format(cal.time)
        returnToDisplayInXxis[i] = dateformat.format(cal.time)
        //Log.d("time",cal.getTime().toString());
        cal = Calendar.getInstance()
        cal.time = sdf.parse(endDate)
    }

    // return new String[]{"","28"};
    return returnToDisplayInXxis
}


private fun setPieLegend() {
    binding.piechart.legend.isEnabled = false
}

private fun setPieChart() {
    binding.piechart.setUsePercentValues(true)
    binding.piechart.getDescription().setEnabled(false)
    binding.piechart.setExtraOffsets(20f, 15f, 20f, 10f)
    binding.piechart.dragDecelerationFrictionCoef = 0.95f

    binding.piechart.setDrawHoleEnabled(true)
    binding.piechart.setHoleColor(MaterialColors.getColor(binding.piechart, R.attr.colorSurface))
    binding.piechart.setTransparentCircleColor(Color.WHITE)
    binding.piechart.setTransparentCircleAlpha(110)

    binding.piechart.setHoleRadius(58f)
    binding.piechart.setTransparentCircleRadius(61f)

    binding.piechart.setDrawCenterText(true)

    binding.piechart.setRotationAngle(0f)
    // enable rotation of the chart by touch
    // enable rotation of the chart by touch
    binding.piechart.setRotationEnabled(false)
    binding.piechart.setHighlightPerTapEnabled(true)

    // entry label styling
    binding.piechart.setEntryLabelColor(Color.WHITE)
    binding.piechart.setEntryLabelTypeface(
        ResourcesCompat.getFont(
            requireContext(),
            R.font.vision_regular
        )
    )
    binding.piechart.setEntryLabelTextSize(12f)

    binding.piechart.animateY(1400, Easing.EaseInOutQuad)

}

private fun setPieData(fc: Double, nc: Double) {
    val entries = ArrayList<PieEntry>()

    if (fc == 0.0 && nc == 0.0)
        return


    entries.add(
        PieEntry(
            fc.toFloat(),
            ""
        )
    )

    entries.add(
        PieEntry(
            nc.toFloat(),
            ""
        )
    )


    val dataSet = PieDataSet(entries, "")

    dataSet.setDrawIcons(false)

    dataSet.sliceSpace = 1f
    dataSet.iconsOffset = MPPointF(0f, 40f)
    dataSet.selectionShift = 3f
    dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
    dataSet.valueLineWidth = 0f
    dataSet.isValueLineVariableLength = false
    dataSet.setUseValueColorForLine(false)
    dataSet.valueLinePart1Length = .4f
    dataSet.valueLinePart2Length = 0f
    dataSet.valueLineColor = ContextCompat.getColor(requireContext(), R.color.white)
    /* dataSet.setValueLinePart1OffsetPercentage(80.f);
    dataSet.setValueLinePart1Length(0.4f);
    dataSet.setValueLinePart2Length(0.5f);*/

    // add a lot of colors

    /* dataSet.setValueLinePart1OffsetPercentage(80.f);
    dataSet.setValueLinePart1Length(0.4f);
    dataSet.setValueLinePart2Length(0.5f);*/

    // add a lot of colors
    val colors = ArrayList<Int>()
    //  colors.add(ColorTemplate.rgb("#CC77700"));
    // colors.add(ColorTemplate.rgb("#F9E5A9"));

    //  colors.add(ColorTemplate.rgb("#CC77700"));
    // colors.add(ColorTemplate.rgb("#F9E5A9"));
    colors.add(ContextCompat.getColor(requireContext(), R.color.pie_fast_dark))
    colors.add(ContextCompat.getColor(requireContext(), R.color.pie_fast_light))
    colors.add(ContextCompat.getColor(requireContext(), R.color.pie_normal_dark))
    colors.add(ContextCompat.getColor(requireContext(), R.color.pie_normal_light))

    dataSet.colors = colors
    //dataSet.setSelectionShift(0f);

    //dataSet.setSelectionShift(0f);
    val data = PieData(dataSet)
    data.setValueFormatter(PercentFormatter())
    data.setValueTextSize(10f)
    data.setValueTextColor(MaterialColors.getColor(binding.piechart, R.attr.colorOnBackground))
    data.setValueTypeface(
        ResourcesCompat.getFont(
            requireContext(),
            R.font.vision_regular
        )
    )
    binding.piechart.data = data

    // undo all highlights

    // undo all highlights
    binding.piechart.highlightValues(null)

    binding.piechart.invalidate()
}

override fun onClick(p0: View?) {
    when (p0!!.id) {
        R.id.iv_back_evscore -> {
            requireActivity().onBackPressed()
        }
    }
}


}
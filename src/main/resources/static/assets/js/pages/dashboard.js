document.addEventListener("DOMContentLoaded", function() {
    var chartDiv = document.getElementById('enrollment-growth-chart');

    // Kiểm tra xem thẻ div có tồn tại không để tránh lỗi JS
    if (chartDiv) {
        // Lấy đúng tên attribute là 'data-chart'
        var rawData = chartDiv.getAttribute('data-chart');
        var dynamicData = JSON.parse(rawData);

        var optionsMonthly = {
            annotations: {
                position: 'back'
            },
            dataLabels: {
                enabled: false
            },
            chart: {
                type: 'bar',
                height: 300
            },
            fill: {
                opacity: 1
            },
            plotOptions: {},
            series: [{
                name: 'Số lượng HV',
                data: dynamicData
            }],
            // Sửa màu sắc thành định dạng Mảng (Array)
            colors: ['#435ebe'],
            xaxis: {
                categories: ["T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"],
            },
        }

        var chartMonthly = new ApexCharts(chartDiv, optionsMonthly);
        chartMonthly.render();
    }
});

document.addEventListener("DOMContentLoaded", function() {
    var chartDiv = document.getElementById('monthly-avg-score');

    if (chartDiv) {
        var rawData = chartDiv.getAttribute('data-score');
        var dynamicData = JSON.parse(rawData);

        var optionsScore = {
            chart: {
                type: 'line', // Dùng biểu đồ đường cho điểm số
                height: 300,
                toolbar: { show: false }
            },
            series: [{
                name: 'Điểm trung bình',
                data: dynamicData
            }],
            stroke: {
                curve: 'smooth', // Đường cong mềm mại
                width: 3
            },
            colors: ['#28c76f'], // Màu xanh lá cho học tập
            markers: {
                size: 4,
                colors: ["#fff"],
                strokeColors: "#28c76f",
                strokeWidth: 2,
            },
            xaxis: {
                categories: ["T1", "T2", "T3", "T4", "T5", "T6", "T7", "T8", "T9", "T10", "T11", "T12"],
            },
            yaxis: {
                min: 0,
                max: 10, // Điểm hệ 10
                tickAmount: 5 // Chia làm 5 mốc: 0, 2, 4, 6, 8, 10
            }
        }

        var chartScore = new ApexCharts(chartDiv, optionsScore);
        chartScore.render();
    }
});
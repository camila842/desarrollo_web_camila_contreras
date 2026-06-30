async function cargarGraficoMiembrosPorDia() {
  try {
    const response = await fetch("/api/miembros-por-dia");
    const data = await response.json();

    const dias = data.map(item => item.dia);
    const cantidades = data.map(item => item.cantidad);

    Highcharts.chart("grafico-miembros", {
      chart: {
        type: "line"
      },
      title: {
        text: "Miembros registrados por día"
      },
      xAxis: {
        categories: dias,
        title: {
          text: "Día"
        }
      },
      yAxis: {
        title: {
          text: "Cantidad de miembros"
        },
        allowDecimals: false
      },
      series: [{
        name: "Miembros registrados",
        data: cantidades
      }]
    });
  } catch (error) {
    console.error("Error al cargar gráfico de miembros:", error);
  }
}
async function cargarGraficoActividadesPorTipo() {
  try {
    const response = await fetch("/api/actividades-por-tipo");
    const data = await response.json();

    const datosTorta = data.map(item => ({
      name: item.tipo,
      y: item.cantidad
    }));

    Highcharts.chart("grafico-actividades-tipo", {
      chart: {
        type: "pie"
      },
      title: {
        text: "Total de actividades extraprogramáticas por tipo"
      },
      tooltip: {
        pointFormat: "{series.name}: <b>{point.y}</b>"
      },
      plotOptions: {
        pie: {
          allowPointSelect: true,
          cursor: "pointer",
          dataLabels: {
            enabled: true,
            format: "{point.name}: {point.y}"
          }
        }
      },
      series: [{
        name: "Actividades",
        colorByPoint: true,
        data: datosTorta
      }]
    });
  } catch (error) {
    console.error("Error al cargar gráfico de actividades por tipo:", error);
  }
}

async function cargarGraficoActividadesPorComuna() {
  try {
    const response = await fetch("/api/actividades-por-comuna");
    const data = await response.json();

    const comunas = data.map(item => item.comuna);
    const cantidades = data.map(item => item.cantidad);

    Highcharts.chart("grafico-actividades-comuna", {
      chart: {
        type: "column"
      },
      title: {
        text: "Total de actividades registradas por comuna"
      },
      xAxis: {
        categories: comunas,
        title: {
          text: "Comuna"
        }
      },
      yAxis: {
        min: 0,
        allowDecimals: false,
        title: {
          text: "Cantidad de actividades"
        }
      },
      series: [{
        name: "Actividades",
        data: cantidades
      }]
    });
  } catch (error) {
    console.error("Error al cargar gráfico de actividades por comuna:", error);
  }
}

cargarGraficoMiembrosPorDia();
cargarGraficoActividadesPorTipo();
cargarGraficoActividadesPorComuna();

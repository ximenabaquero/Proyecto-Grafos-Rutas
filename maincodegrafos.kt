import java.util.PriorityQueue

// ---------------------------------------------------------------------------
// 1. DEFINICIÓN DEL GRAFO
// ---------------------------------------------------------------------------
// Mapa: cada nodo -> lista de (vecino, peso en minutos)
// Diseñado para que A->D = 32 y B->E = 27
val GRAFO: Map<String, List<Pair<String, Int>>> = mapOf(
    "A" to listOf("B" to 15, "C" to 20, "F" to 35),
    "B" to listOf("D" to 25, "C" to 10, "E" to 27),
    "C" to listOf("D" to 12, "F" to 15, "E" to 30),
    "D" to listOf("E" to 10, "F" to 10),
    "E" to listOf("F" to 5, "G" to 12),
    "F" to listOf("G" to 8),
    "G" to emptyList()
)

val NOMBRES: Map<String, String> = mapOf(
    "A" to "Parque de la 93",
    "B" to "Plaza de Bolívar",
    "C" to "Usaquén",
    "D" to "Museo Nacional",
    "E" to "Jardín Botánico",
    "F" to "Andino",
    "G" to "Universidad Nacional"
)

// ---------------------------------------------------------------------------
// 2. ALGORITMO DE DIJKSTRA (EXPLICADO)
// ---------------------------------------------------------------------------
/**
 * Calcula distancias mínimas desde [origen] a todos los nodos.
 * @param verPasos si es true imprime cada intento de relajación.
 * @return Pair(distancias, padres)
 */
fun dijkstra(
    grafo: Map<String, List<Pair<String, Int>>>,
    origen: String,
    verPasos: Boolean = false
): Pair<MutableMap<String, Int>, MutableMap<String, String?>> {

    // Dijkstra en una frase (mi resumen):
    // "Voy expandiendo siempre el camino más corto conocido y veo si puedo mejorar
    // (relajar) las distancias a los vecinos".

    // 1. Inicializamos distancias en infinito, excepto el origen
    val distancias = mutableMapOf<String, Int>()
    val padres = mutableMapOf<String, String?>()
    for (n in grafo.keys) {
        distancias[n] = Int.MAX_VALUE
        padres[n] = null
    }
    distancias[origen] = 0

    // 2. Cola de prioridad (menor distancia primero)
    data class Estado(val dist: Int, val nodo: String)
    val cola = PriorityQueue<Estado>(compareBy { it.dist })
    cola.add(Estado(0, origen))

    if (verPasos) println("\n[INICIO DIJKSTRA]")

    while (cola.isNotEmpty()) { // Mientras queden candidatos
        val actual = cola.poll()
        val nodo = actual.nodo
        val distActual = actual.dist

        if (verPasos) println("Procesando nodo $nodo con distancia $distActual")

        // Si ya tenemos algo mejor, saltamos
        if (distActual > distancias[nodo]!!) continue

        // Revisar vecinos (aquí ocurre la "relajación")
        val vecinos = grafo[nodo]
        for ((vecino, peso) in if (vecinos != null) vecinos else emptyList()) {
            val nuevaDist = distActual + peso
            if (verPasos) println("  Vecino $vecino: actual=${distancias[vecino]} nueva=$nuevaDist")

            if (nuevaDist < distancias[vecino]!!) {
                distancias[vecino] = nuevaDist
                padres[vecino] = nodo
                cola.add(Estado(nuevaDist, vecino))
                if (verPasos) println("    Actualizado $vecino -> $nuevaDist (padre=$nodo)")
            }
        }
    }

    if (verPasos) println("[FIN DIJKSTRA]\n")

    return distancias to padres
}

// ---------------------------------------------------------------------------
// 3. RECONSTRUCCIÓN DE RUTA
// ---------------------------------------------------------------------------
fun reconstruirRuta(
    padres: Map<String, String?>,
    origen: String,
    destino: String
): List<String>? {
    // Si no tiene padre y no es el origen -> no hay ruta.
    if (padres[destino] == null && destino != origen) return null
    val ruta = mutableListOf<String>()
    var actual: String? = destino
    while (actual != null) {
        ruta.add(actual)
        actual = padres[actual]
    }
    ruta.reverse()
    return if (ruta.first() == origen) ruta else null
}

// Explicación breve imprimible (la puedo llamar desde el menú si quiero)
fun explicarDijkstraBreve() {
    println("\nExplicación rapida de Dijkstra (versión estudiante):")
    println("1. Empiezo con distancia 0 en el origen y ∞ en los demás.")
    println("2. Siempre tomo el nodo pendiente con menor distancia.")
    println("3. Intento mejorar (relajar) a cada vecino: dist[nuevo] = dist[actual] + peso.")
    println("4. Si mejoro una distancia, guardo quién fue su 'padre'.")
    println("5. Repito hasta que no hay nodos en la cola.")
    println("6. Para reconstruir la ruta: voy desde el destino hacia atrás usando padres[].")
}

// ---------------------------------------------------------------------------
// 4. UTILIDADES INTERACTIVAS
// ---------------------------------------------------------------------------
fun mostrarUbicaciones() {
    println("\nUbicaciones disponibles:")
    for (k in GRAFO.keys.sorted()) {
        println("  $k - ${NOMBRES[k]}")
    }
}

fun opcionRutaMasCorta() {
    mostrarUbicaciones()
    print("\nOrigen: ")
    val origenInput = readLine()?.trim()?.uppercase()
    if (origenInput == null) return
    val origen = origenInput
    print("Destino: ")
    val destinoInput = readLine()?.trim()?.uppercase()
    if (destinoInput == null) return
    val destino = destinoInput

    if (!GRAFO.containsKey(origen) || !GRAFO.containsKey(destino)) {
        println(" Nodo inválido")
        return
    }

    print("¿Ver pasos internos de Dijkstra? (s/N): ") // Recomiendo probar 's' al menos una vez
    val ver = readLine()?.trim()?.lowercase() == "s"

    val (dist, padres) = dijkstra(GRAFO, origen, verPasos = ver)
    val ruta = reconstruirRuta(padres, origen, destino)

    if (ruta == null) {
        println("No existe ruta")
        return
    }

    println("\nResultado:")
    println("  Ruta: ${ruta.joinToString(" -> ")}")
    println("  Tiempo total: ${dist[destino]} minutos")

    // Tabla de segmentos
    println("\nSegmentos (para entender qué suma cada tramo):")
    var acumulado = 0
    for (i in 0 until ruta.size - 1) {
        val a = ruta[i]
        val b = ruta[i + 1]
        val peso = GRAFO[a]!!.first { it.first == b }.second
        acumulado += peso
        println("  $a -> $b: $peso (acumulado: $acumulado)")
    }
}

fun opcionTodasLasDistancias() {
    mostrarUbicaciones()
    print("\nOrigen: ")
    val origenInput = readLine()?.trim()?.uppercase()
    if (origenInput == null) return
    val origen = origenInput
    if (!GRAFO.containsKey(origen)) {
        println("Nodo inválido")
        return
    }
    val (dist, padres) = dijkstra(GRAFO, origen)
    println("\nDistancias mínimas desde $origen:")
    for (n in GRAFO.keys.sorted()) {
        val d = dist[n]
        val ruta = reconstruirRuta(padres, origen, n)
        if (ruta != null) {
            println("  $origen -> $n: $d min | Ruta: ${ruta.joinToString(" -> ")}")
        }
    }
}

fun menu() {
    while (true) {
        println("\n" + "=".repeat(55))
        println("SISTEMA DE RUTAS - VERSION ESTUDIANTES (KOTLIN)")
        println("=".repeat(55))
        println("1. Ruta más corta entre dos puntos")
        println("2. Ver todas las distancias desde un origen")
        println("3. Ver ubicaciones")
        println("4. Explicación breve de Dijkstra")
        println("5. Ver retos sugeridos")
        println("6. Salir")
        print("\nElige una opción (1-4): ")
        when (readLine()?.trim()) {
            "1" -> opcionRutaMasCorta()
            "2" -> opcionTodasLasDistancias()
            "3" -> mostrarUbicaciones()
            "4" -> explicarDijkstraBreve()
            "5" -> retos()
            "6" -> { println("\n¡Hasta luego! (Recuerda: cambia un peso y prueba otra vez.)"); return }
            else -> println("Opción inválida (intenta 1..6)")
        }
    }
}

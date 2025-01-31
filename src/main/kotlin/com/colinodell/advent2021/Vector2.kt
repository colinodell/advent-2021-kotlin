package com.colinodell.advent2021

import kotlin.math.abs
import kotlin.math.max

data class Vector2 (val x: Int, val y: Int) {
    operator fun plus(other: Vector2) = Vector2(x + other.x, y + other.y)

    operator fun minus(other: Vector2) = Vector2(x - other.x, y - other.y)

    override fun toString() = "($x, $y)"

    fun neighbors() = listOf(
        Vector2(x - 1, y),
        Vector2(x + 1, y),
        Vector2(x, y - 1),
        Vector2(x, y + 1)
    )

    fun neighborsIncludingDiagonals() = neighbors() + listOf(
        Vector2(x - 1, y - 1),
        Vector2(x - 1, y + 1),
        Vector2(x + 1, y - 1),
        Vector2(x + 1, y + 1)
    )

    // Unlike the other functions, the order here is important!
    fun allNeighborsIncludingSelf() = listOf(
        Vector2(x - 1, y - 1),
        Vector2(x, y - 1),
        Vector2(x + 1, y - 1),
        Vector2(x - 1, y),
        this,
        Vector2(x + 1, y),
        Vector2(x - 1, y + 1),
        Vector2(x, y + 1),
        Vector2(x + 1, y + 1)
    )
}

// A line that is at some multiple of 45 degrees (horizontal, vertical, or diagonal)
data class Line(val start: Vector2, val end: Vector2) {
    val points : List<Vector2> by lazy {
        val xDiff = end.x - start.x
        val yDiff = end.y - start.y
        val stepCount = max(abs(xDiff), abs(yDiff))

        val xStep = xDiff / stepCount
        val yStep = yDiff / stepCount

        (0 .. stepCount).map { Vector2(start.x + it * xStep, start.y + it * yStep) }
    }

    val isHorizontal : Boolean by lazy { start.y == end.y }
    val isVertical : Boolean by lazy { start.x == end.x }
    val isDiagonal : Boolean by lazy { ! (isHorizontal || isVertical) }
}

data class Region(val topLeft: Vector2, val bottomRight: Vector2) {
    operator fun contains(point: Vector2): Boolean = point.x in topLeft.x..bottomRight.x && point.y in topLeft.y..bottomRight.y
}

typealias Grid<T> = Map<Vector2, T>
data class GridEntry<V>(override val key: Vector2, override val value: V?) : Map.Entry<Vector2, V?>

fun <T> Grid<T>.neighborsOf(point: Vector2): Map<Vector2, T> {
    return point.neighbors().filter { containsKey(it) }.associateWith { get(it)!! }
}
fun <T> Grid<T>.neighborsIncludingDiagonalsOf(point: Vector2): Map<Vector2, T> {
    return point.neighborsIncludingDiagonals().filter { containsKey(it) }.associateWith { get(it)!! }
}

fun <T> Grid<T>.width() = keys.maxOf { it.x } - keys.minOf { it.x } + 1
fun <T> Grid<T>.height() = keys.maxOf { it.y } - keys.minOf { it.y } + 1

fun <T> Grid<T>.topLeft() = Vector2(keys.minOf { it.x }, keys.minOf { it.y })
fun <T> Grid<T>.bottomRight() = Vector2(keys.maxOf { it.x }, keys.maxOf { it.y })

fun <T, O> Grid<T>.mapAllPositions(padding: Int = 0, transform: (GridEntry<T>) -> O): Grid<O> {
    val grid = mutableMapOf<Vector2, O>()

    (keys.minOf { it.y } - padding .. keys.maxOf { it.y } + padding).forEach { y ->
        (keys.minOf { it.x } - padding..keys.maxOf { it.x } + padding).forEach { x ->
            grid[Vector2(x, y)] = transform(GridEntry(Vector2(x, y), this[Vector2(x, y)]))
        }
    }

    return grid
}

fun Collection<Vector2>.toStringVisualization(): String {
    val minX = minOf { it.x }
    val minY = minOf { it.y }
    val maxX = maxOf { it.x }
    val maxY = maxOf { it.y }

    val grid = Array(maxY - minY + 1) { Array(maxX - minX + 1) { '.' } }

    for (point in this) {
        grid[point.y - minY][point.x - minX] = '#'
    }

    return grid.map { it.joinToString("") }.joinToString("\n")
}

fun <T> Grid<T>.toStringVisualization(): String {
    val minX = minOf { it.key.x }
    val minY = minOf { it.key.y }
    val maxX = maxOf { it.key.x }
    val maxY = maxOf { it.key.y }

    val grid = Array(maxY - minY + 1) { Array(maxX - minX + 1) { '.' } }

    for (point in this) {
        grid[point.key.y - minY][point.key.x - minX] = point.value.toString()[0]
    }

    return grid.map { it.joinToString("") }.joinToString("\n")
}

fun <T> List<String>.toGrid(transform: (Char) -> T) = mutableMapOf<Vector2, T>().apply {
    forEachIndexed { y, line ->
        line.forEachIndexed { x, c ->
            put(Vector2(x, y), transform(c))
        }
    }
}
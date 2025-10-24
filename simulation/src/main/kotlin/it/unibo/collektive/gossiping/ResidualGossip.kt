package it.unibo.collektive.gossiping

import it.unibo.collektive.aggregate.api.Aggregate
import it.unibo.collektive.aggregate.api.exchange
import it.unibo.collektive.aggregate.api.exchanging
import it.unibo.collektive.aggregate.api.neighborhood
import it.unibo.collektive.stdlib.pairs.FieldedPairs.first
import kotlin.random.Random

/**
 * If the node is the source, it returns 0, otherwise it returns the number of neighbors.
 */
fun Aggregate<Int>.residualGossip(x: Double, y: Double, alpha: Double, gamma: Double): Double {
    return exchange(x to y) { residualInformation ->
        val (xLocal, yLocal) = residualInformation.local.value
        val (xNew, yNew) = residualInformation.neighbors.list
            .fold(xLocal to yLocal) { acc, neighborInfo ->
                residualAggregateUpdate(
                    sender = neighborInfo.value,
                    receiverAcc = acc,
                    alpha = alpha,
                    gamma = gamma,
                    numNeighbors = residualInformation.neighbors.list.size
                )
            }
        val result = residualInformation.map {
            if(it.id == localId) {
                xNew to (yNew * alpha)
            } else {
                xNew to yNew
            }
        }
        result
    }.local.value.first
}

private fun residualAggregateUpdate(
    sender: Pair<Double, Double>,
    receiverAcc: Pair<Double, Double>,
    alpha: Double,
    gamma: Double,
    numNeighbors: Int
): Pair<Double, Double> {
    val (xS, yS) = sender
    val (xR, yR) = receiverAcc
    val n = numNeighbors.toDouble()

    // Formula aggregata: normalizza per numero di vicini
    val xUpdated = xR - (gamma / n) * (xR - xS) + ((1.0 - gamma) * (1.0 - alpha) / n) * yS
    val yUpdated = yR + (gamma / n) * (xR - xS) + (gamma * (1.0 - alpha) / n) * yS

    return xUpdated to yUpdated
}


/**
 * The entrypoint of the simulation running a branching program.
 */
fun Aggregate<Int>.residualGossipEntrypoint(): Double =
    residualGossip(Random.nextDouble(), 0.0, 0.1, 0.4)

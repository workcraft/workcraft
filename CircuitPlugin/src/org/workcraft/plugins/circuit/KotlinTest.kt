@file:JvmName("KotlinTest")

package org.workcraft.plugins.circuit

fun print(s: String) {
	System.out.println(s)
}

fun printAll(vararg args: String) {
	for (arg in args) System.out.println(arg)
}

package com.example.persistent

case class CalculationResult(result: Double = 0) {
  def reset: CalculationResult = copy(result = 0)
  def add(value: Double): CalculationResult = copy(result = this.result + value)
  def subtract(value: Double): CalculationResult = copy(result = this.result - value)
  def divide(value: Double): CalculationResult = copy(result = this.result / value)
  def multiply(value: Double): CalculationResult = copy(result = this.result * value)
}
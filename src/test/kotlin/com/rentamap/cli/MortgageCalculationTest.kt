package com.rentamap.cli

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.doubles.shouldBeGreaterThan
import io.kotest.matchers.doubles.shouldBeLessThan
import io.kotest.matchers.shouldBe

class MortgageCalculationTest : StringSpec({

    val calculator = FrenchPropertyInvestmentCalculator()

    "monthly payment should match expected amortization formula" {
        val input = PropertyInvestmentInput(
            propertyPrice = 240000.0,
            downPayment = 0.0,
            interestRate = 3.5,
            loanTermYears = 20,
            monthlyRent = 1500.0,
            holdingPeriodYears = 1
        )

        val result = calculator.analyze(input)

        val principal = 240000.0
        val monthlyRate = 3.5 / 100.0 / 12.0
        val numPayments = 20 * 12
        val basePayment = principal * (monthlyRate * Math.pow(1 + monthlyRate, numPayments.toDouble())) /
                         (Math.pow(1 + monthlyRate, numPayments.toDouble()) - 1)
        val lifeInsurance = (principal * 0.004) / 12.0
        val expectedMonthlyPayment = basePayment + lifeInsurance

        result.mortgage.monthlyPayment shouldBe (expectedMonthlyPayment plusOrMinus 0.01)
    }

    "interest payments should decrease over time" {
        val input = PropertyInvestmentInput(
            propertyPrice = 300000.0,
            downPayment = 60000.0,
            interestRate = 4.0,
            loanTermYears = 20,
            monthlyRent = 1500.0,
            holdingPeriodYears = 10
        )

        val result = calculator.analyze(input)

        val loanAmount = 240000.0
        val monthlyRate = 4.0 / 100.0 / 12.0
        val monthlyPayment = result.mortgage.monthlyPayment

        var previousYearInterest = Double.MAX_VALUE

        for (i in 0 until 10.coerceAtMost(result.yearlyProjections.size)) {
            val year = result.yearlyProjections[i]
            var balance = loanAmount - result.yearlyProjections.take(i).sumOf { it.principalPaydown }
            var yearlyInterest = 0.0

            for (month in 1..12) {
                if (balance <= 0) break
                val interestPayment = balance * monthlyRate
                yearlyInterest += interestPayment
                val principalPayment = monthlyPayment - interestPayment
                balance -= principalPayment
            }

            if (i > 0) {
                yearlyInterest shouldBeLessThan previousYearInterest
            }
            previousYearInterest = yearlyInterest
        }
    }

    "principal payments should increase over time" {
        val input = PropertyInvestmentInput(
            propertyPrice = 300000.0,
            downPayment = 60000.0,
            interestRate = 4.0,
            loanTermYears = 20,
            monthlyRent = 1500.0,
            holdingPeriodYears = 10
        )

        val result = calculator.analyze(input)

        for (i in 1 until result.yearlyProjections.size) {
            val currentYear = result.yearlyProjections[i]
            val previousYear = result.yearlyProjections[i - 1]

            if (currentYear.principalPaydown > 0 && previousYear.principalPaydown > 0) {
                currentYear.principalPaydown shouldBeGreaterThan previousYear.principalPaydown
            }
        }
    }

    "principal paydown should sum to loan amount over full loan term" {
        val input = PropertyInvestmentInput(
            propertyPrice = 300000.0,
            downPayment = 60000.0,
            interestRate = 3.5,
            loanTermYears = 15,
            monthlyRent = 1500.0,
            holdingPeriodYears = 15
        )

        val result = calculator.analyze(input)

        val loanAmount = 240000.0
        val totalPrincipalPaid = result.summary.totalPrincipalPaydown

        totalPrincipalPaid shouldBeGreaterThan (loanAmount - 100.0)
        totalPrincipalPaid shouldBeLessThan (loanAmount + 1000.0)
        result.summary.finalEquity shouldBe (input.downPayment + result.summary.totalPrincipalPaydown plusOrMinus 1.0)
    }

    "total payments should equal principal plus total interest" {
        val input = PropertyInvestmentInput(
            propertyPrice = 300000.0,
            downPayment = 60000.0,
            interestRate = 3.5,
            loanTermYears = 20,
            monthlyRent = 1500.0,
            holdingPeriodYears = 20
        )

        val result = calculator.analyze(input)

        val loanAmount = 240000.0
        val expectedTotalPayments = loanAmount + result.mortgage.totalInterest

        result.mortgage.totalPayments shouldBe (expectedTotalPayments plusOrMinus 0.01)
    }

    "remaining balance should decrease each year" {
        val input = PropertyInvestmentInput(
            propertyPrice = 300000.0,
            downPayment = 60000.0,
            interestRate = 3.5,
            loanTermYears = 20,
            monthlyRent = 1500.0,
            holdingPeriodYears = 10
        )

        val result = calculator.analyze(input)

        var previousBalance = 240000.0
        for (year in result.yearlyProjections) {
            year.remainingBalance shouldBeLessThan previousBalance
            previousBalance = year.remainingBalance
        }
    }

    "stored mortgage calculation values for 240k loan at 3.5% over 20 years" {
        val input = PropertyInvestmentInput(
            propertyPrice = 240000.0,
            downPayment = 0.0,
            interestRate = 3.5,
            loanTermYears = 20,
            monthlyRent = 1500.0,
            holdingPeriodYears = 20
        )

        val result = calculator.analyze(input)

        result.mortgage.monthlyPayment shouldBe (1471.90 plusOrMinus 1.0)
        result.mortgage.totalInterest shouldBe (113256.80 plusOrMinus 200.0)
        result.mortgage.totalPayments shouldBe (353256.80 plusOrMinus 200.0)

        result.yearlyProjections[0].principalPaydown shouldBe (9412.89 plusOrMinus 20.0)
        result.yearlyProjections[9].principalPaydown shouldBe (12892.18 plusOrMinus 20.0)

        result.summary.totalPrincipalPaydown shouldBe (240000.0 plusOrMinus 1000.0)
    }

    "stored mortgage calculation values for 300k loan at 4.0% over 15 years" {
        val input = PropertyInvestmentInput(
            propertyPrice = 300000.0,
            downPayment = 0.0,
            interestRate = 4.0,
            loanTermYears = 15,
            monthlyRent = 2000.0,
            holdingPeriodYears = 15
        )

        val result = calculator.analyze(input)

        result.mortgage.monthlyPayment shouldBe (2319.06 plusOrMinus 1.0)
        result.mortgage.totalInterest shouldBe (117431.48 plusOrMinus 200.0)
        result.mortgage.totalPayments shouldBe (417431.48 plusOrMinus 200.0)

        result.yearlyProjections[0].principalPaydown shouldBe (16122.21 plusOrMinus 20.0)
        result.yearlyProjections[7].principalPaydown shouldBe (21321.84 plusOrMinus 20.0)

        result.summary.totalPrincipalPaydown shouldBe (300000.0 plusOrMinus 1500.0)
    }

    "stored mortgage calculation values for 180k loan at 3.0% over 25 years" {
        val input = PropertyInvestmentInput(
            propertyPrice = 180000.0,
            downPayment = 0.0,
            interestRate = 3.0,
            loanTermYears = 25,
            monthlyRent = 1200.0,
            holdingPeriodYears = 25
        )

        val result = calculator.analyze(input)

        result.mortgage.monthlyPayment shouldBe (913.58 plusOrMinus 1.0)
        result.mortgage.totalInterest shouldBe (94074.11 plusOrMinus 200.0)
        result.mortgage.totalPayments shouldBe (274074.11 plusOrMinus 200.0)

        result.yearlyProjections[0].principalPaydown shouldBe (5640.10 plusOrMinus 20.0)
        result.yearlyProjections[12].principalPaydown shouldBe (8080.48 plusOrMinus 20.0)

        result.summary.totalPrincipalPaydown shouldBe (180000.0 plusOrMinus 300.0)
    }

    "interest portion decreases while principal portion increases within each year" {
        val input = PropertyInvestmentInput(
            propertyPrice = 300000.0,
            downPayment = 60000.0,
            interestRate = 4.5,
            loanTermYears = 20,
            monthlyRent = 1500.0,
            holdingPeriodYears = 5
        )

        val result = calculator.analyze(input)

        val loanAmount = 240000.0
        val monthlyRate = 4.5 / 100.0 / 12.0
        val monthlyPayment = result.mortgage.monthlyPayment

        for (yearIndex in 0 until result.yearlyProjections.size) {
            var balance = loanAmount - result.yearlyProjections
                .take(yearIndex)
                .sumOf { it.principalPaydown }

            var firstMonthInterest = 0.0
            var lastMonthInterest = 0.0
            var firstMonthPrincipal = 0.0
            var lastMonthPrincipal = 0.0

            for (month in 1..12) {
                if (balance <= 0) break

                val interestPayment = balance * monthlyRate
                val principalPayment = monthlyPayment - interestPayment

                if (month == 1) {
                    firstMonthInterest = interestPayment
                    firstMonthPrincipal = principalPayment
                }
                if (month == 12 || balance - principalPayment <= 0) {
                    lastMonthInterest = interestPayment
                    lastMonthPrincipal = principalPayment
                }

                balance -= principalPayment
            }

            if (lastMonthInterest > 0 && firstMonthInterest > 0) {
                lastMonthInterest shouldBeLessThan firstMonthInterest
                lastMonthPrincipal shouldBeGreaterThan firstMonthPrincipal
            }
        }
    }

    "mortgage with short holding period should have remaining balance" {
        val input = PropertyInvestmentInput(
            propertyPrice = 300000.0,
            downPayment = 60000.0,
            interestRate = 3.5,
            loanTermYears = 20,
            monthlyRent = 1500.0,
            holdingPeriodYears = 5
        )

        val result = calculator.analyze(input)

        val loanAmount = 240000.0
        result.yearlyProjections.last().remainingBalance shouldBeGreaterThan 0.0
        result.yearlyProjections.last().remainingBalance shouldBeLessThan loanAmount
    }

    "total interest calculation should be accurate" {
        val input = PropertyInvestmentInput(
            propertyPrice = 200000.0,
            downPayment = 40000.0,
            interestRate = 3.5,
            loanTermYears = 20,
            monthlyRent = 1200.0,
            holdingPeriodYears = 20
        )

        val result = calculator.analyze(input)

        val loanAmount = 160000.0
        val totalPaid = result.mortgage.monthlyPayment * 20 * 12
        val expectedInterest = totalPaid - loanAmount

        result.mortgage.totalInterest shouldBe (expectedInterest plusOrMinus 1.0)
    }
})

package BasicDesigns
import chisel3._
import chisel3.util._
import chisel3.tester.RawTester.test
import firrtl._
import scala.collection._
import chisel3.tester._
// this is just a collection of typical circuits like the full adder, full subtract, full multiply, shift, and more
object Arithmetic {

  // for detecting the most significant 1. I use this for my FP_adder for determining shift amounts
  class leadingOneDetector(bw: Int) extends Module{
    //require(bw == 11 || bw == 24 || bw == 53 || bw ==113) // size of the mantissa + 1
    val io = IO(new Bundle() { // one input, one output
      val in = Input(UInt(bw.W))
      val out = Output(UInt((log2Floor(bw) + 1).W))
    })
    val boolseq = for(i <- 0 until bw) yield {
      (io.in(bw-1-i), (bw-i).U)
    }
    val hotValue = PriorityMux(boolseq.toSeq)
    io.out := hotValue
  }

  class twoscomplement(bw: Int) extends Module{
    val io = IO(new Bundle() {
      val in = Input(UInt(bw.W))
      val out = Output(UInt(bw.W))
    })
    val x = Wire(UInt(bw.W))
    x := 1.U + ~(io.in)
    io.out := x
  }

  class shifter(bw: Int) extends Module{
    val io = IO(new Bundle() {
      val in_a = Input(UInt(bw.W))
      val in_b = Input(UInt((log2Floor(bw) + 1).W))
      val in_c = Input(UInt(1.W))
      val out_s = Output(UInt(bw.W))
    })
    val result = Wire(UInt(bw.W))
    result := 0.U
    when(io.in_c === 1.U){ // shift right
      result := io.in_a >> io.in_b
    }.otherwise{
      result := io.in_a << io.in_b // shift left
    }
    io.out_s := result
  }

  class full_adder(bw: Int) extends Module{
    val io = IO(new Bundle() {
      val in_a = Input(UInt(bw.W))
      val in_b = Input(UInt(bw.W))
      val in_c = Input(UInt(1.W))
      val out_s = Output(UInt(bw.W))
      val out_c = Output(UInt(1.W))
    })
    val result = Wire(UInt((bw+1).W))
    result := io.in_a +& io.in_b +& io.in_c
    io.out_s := result(bw-1,0)
    io.out_c := result(bw)
  }

  class full_subber(bw: Int) extends Module{
    val io = IO(new Bundle() {
      val in_a = Input(UInt(bw.W))
      val in_b = Input(UInt(bw.W))
      val in_c = Input(UInt(1.W))
      val out_s = Output(UInt(bw.W))
      val out_c = Output(UInt(1.W))
    })
    val result = Wire(UInt((bw+1).W))
    result := io.in_a -& io.in_b -& io.in_c
    io.out_s := result(bw-1,0)
    io.out_c := result(bw)
  }

  class multiplier(bw: Int) extends Module{
    val io = IO(new Bundle() {
      val in_a = Input(UInt(bw.W))
      val in_b = Input(UInt(bw.W))
      val out_s = Output(UInt((bw*2).W))
    })
    val result = Wire(UInt((bw*2).W))
    result := io.in_a * io.in_b
    io.out_s := result
  }

  class multiplier2(bw: Int) extends Module{
    val io = IO(new Bundle() {
      val in_a = Input(UInt(bw.W))
      val in_b = Input(UInt(bw.W))
      val out_s = Output(UInt((bw*2).W))
    })
    val in_reg = Reg(Vec(2, UInt(bw.W)))
    val out_reg = Reg(UInt((bw*2).W))
    in_reg(0) := io.in_a
    in_reg(1) := io.in_b
    val result = Wire(UInt((bw*2).W))
    result := in_reg(0) * in_reg(1)
    out_reg := result
    io.out_s := out_reg
  }
}

class division(bw: Int) extends Module{
  val io = IO(new Bundle() {
    val in_a = Input(UInt(bw.W))
    val in_b = Input(UInt(bw.W))
    val out_s = Output(UInt((bw*2).W))
  })
  val result = Wire(UInt((bw*2).W))
  result := io.in_a / io.in_b
  io.out_s := result
}



/*
object tester_1 {

  def main(args: Array[String]): Unit = {
    test(new division(16)) { c =>
      c.io.in_a.poke(10.U)
      c.io.in_b.poke(2.U)
      c.io.out_s.expect(5.U)

    }
    println("SUCCESS!!")
  }
}
 */
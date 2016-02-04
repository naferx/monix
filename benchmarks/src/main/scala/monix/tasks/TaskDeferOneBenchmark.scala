/*
 * Copyright (c) 2014-2016 by its authors. Some rights reserved.
 * See the project homepage at: https://monix.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package monix.tasks

import java.util.concurrent.TimeUnit
import monix.execution.Scheduler.Implicits.global
import monix.tasks.{Task => MonixTask}
import org.openjdk.jmh.annotations._
import scalaz.concurrent.{Task => ScalazTask}
import scalaz.{-\/, \/-}

/*
 * Sample run:
 *
 *     sbt "benchmarks/jmh:run -i 10 -wi 10 -f 1 -t 1 monix.tasks.TaskDeferOneBenchmark"
 *
 * Which means "10 iterations" "5 warmup iterations" "1 fork" "1 thread".
 * Please note that benchmarks should be usually executed at least in
 * 10 iterations (as a rule of thumb), but more is better.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class TaskDeferOneBenchmark {
  @Benchmark
  def monix(): Long = {
    var result = 0
    MonixTask.eval(10).runAsync(new Callback[Int] {
      def onError(ex: Throwable): Unit = throw ex
      def onSuccess(value: Int): Unit =
        result = value
    })
    result
  }

  @Benchmark
  def scalaz(): Long = {
    var result = 0
    ScalazTask.delay(10).unsafePerformAsync {
      case -\/(ex) => throw ex
      case \/-(value) => result = value
    }
    result
  }
}
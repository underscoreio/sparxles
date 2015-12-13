package sparxles

sealed trait EventStream[A] {
  def map[B](f: A => B): EventStream[B]
  def scan[B](seed: B)(f: (B,A) => B): EventStream[B]
  def join[B](that: EventStream[B]): EventStream[(A,B)]
}
object EventStream {
  final case class Map[A,B](source: EventStream[A], f: A => B) extends EventStream[B]
  final case class Scan[A,B](source: EventStream[A], seed: B, f: (B,A) => B) extends EventStream[B]
  final case class Join[A,B](left: EventStream[A], right: EventStream[B]) extends EventStream[(A,B)]
  final case class Emit[A](events: List[A]) extends EventStream[A]

  def emit[A](event: A, events: A*): EventStream[A] =
    Emit(event :: events.toList)
}

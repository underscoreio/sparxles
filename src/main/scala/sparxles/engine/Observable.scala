package sparxles
package engine

sealed trait Observable[A] {
  def observe(observer: Observer[A])
  def map[B](f: A => B): Observable[B]
  def scan[B](seed: B)(f: (B,A) => B): Observable[B]
}
sealed trait Observer[A] {
  def onNext[A]: Unit
  def onComplete(): Unit
}

object Observable {
  def run[A](stream: EventStream[A]): A = {
    var translations: Map[EventStream[_], Observable[_]] = Map.empty
    def getTranslation[A](stream: EventStream[A]): Option[Observable[A]] =
      translations.get(stream).map(_.asInstanceOf[Observable[A]])
    def addTranslation[A](stream: EventStream[A], obs: Observable[A]): Unit =
      translations = translations + (stream -> obs)

    def toObservable[A](stream: EventStream[A]): Observable[A] = {
      import EventStream._

      val obs: Observable[A] =
        stream match {
          case map: Map[a,b] =>
            val parent: EventStream[a] = map.source
            getTranslation(parent) match {
              case Some(p) =>
                p.map(map.f)
              case None =>
                val p = toObservable(parent)
                p.map(map.f)
            }

          case scan: Scan[a,b] =>
            val parent: EventStream[a] = scan.source
            getTranslation(parent) match {
              case Some(p) =>
                p.scan(scan.seed)(scan.f)
              case None =>
                val p = toObservable(parent)
                p.scan(scan.seed)(scan.f)
            }

          case join: Join[a, b] =>
            ???

          case Emit(f) =>
            ???
        }
      addTranslation(stream, obs)
      obs
    }

    toObservable(stream)
  }
}

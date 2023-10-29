(ns sliding-puzzle.easing-functions)

(defn ease-in-out-cubic [x]
  (if (< x 0.5)
    (* 4 x x x)
    (let [x' (+ (* -2 x) 2)]
      (- 1 (* x' x' x' 0.5)))))

(defn ease-out-cubic [x]
  (let [x' (- 1 x)]
    (- 1 (* x' x' x'))))

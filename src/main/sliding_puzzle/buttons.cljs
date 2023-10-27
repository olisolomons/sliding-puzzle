(ns sliding-puzzle.buttons
  (:require
   [quil.core :as q]))

(defn draw-buttons [{:keys [buttons canvas-size]}]
  (q/fill 150)
  (q/no-stroke)
  (let [r (* canvas-size 0.02)]
    (doseq [{[x y width height] :rect}  buttons]
      (q/rect x y width height r))
    (q/fill 50)
    (q/text-align :center :center)
    (doseq [{:keys [text text-size] [x y width height] :rect}  buttons]
      (q/text-size text-size)
      (q/text text (+ x (* width 0.5)) (+ y (* height 0.5))))))

(defn get-clicked-button-fn [{:keys [buttons]} click-x click-y]
  (some 
    (fn [{[x y width height] :rect :as button}]
      (when (and (<= 0 (- click-x x) width)
                 (<= 0 (- click-y y) height))
        (:fn button)))
    buttons))

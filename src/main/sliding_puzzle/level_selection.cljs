(ns sliding-puzzle.level-selection
  (:require
   [quil.core :as q]
   [sliding-puzzle.buttons :as buttons]
   [sliding-puzzle.sketch-functions :refer [add-derived-state draw-state
                                            key-pressed mouse-dragged mouse-pressed
                                            update-state]]
   [sliding-puzzle.levels :as levels]
   [sliding-puzzle.screen-transition :as screen-transition]))

(defn mk-state [canvas-size main-menu]
  (add-derived-state
   {:type :level-selection
    :main-menu main-menu
    :canvas-size canvas-size}))

(defmethod add-derived-state :level-selection
  [{:keys [canvas-size main-menu] :as state}]
  (let [p #(* canvas-size %)
        margin 0.2
        padding 0.02
        box-width (/ (- 1 (* 2 margin)) 4)]
    (assoc state
           :buttons
           (cons
            {:rect (map p [0.05 0.05 0.2 0.1])
             :fn (fn [state] (screen-transition/mk-state state main-menu :left))
             :text "Back"
             :text-size (* canvas-size 0.04)}
            (map-indexed
             (fn [i level]
               (let [x (mod i 4)
                     y (int (/ i 4))]
                 {:rect (map p [(+ (* box-width x) margin padding)
                                (+ (* box-width y) margin padding)
                                (- box-width padding padding)
                                (- box-width padding padding)])
                  :fn (fn [state]
                        (screen-transition/mk-state state level :right))
                  :text (str (inc i))}))
             levels/levels)))))

(defmethod mouse-dragged :level-selection
  [state]
  state)

(defmethod update-state :level-selection
  [state]
  state)

(defmethod draw-state :level-selection
  [state]
  (buttons/draw-buttons state))

(defmethod mouse-pressed :level-selection
  [state {:keys [x y]}]
  (if-let [button-fn (buttons/get-clicked-button-fn state x y)]
    (button-fn state)
    state))

(defmethod key-pressed :level-selection
  [{:keys [main-menu] :as state} event]
  (case (:key event)
    :Escape (screen-transition/mk-state state main-menu :left)
    state))


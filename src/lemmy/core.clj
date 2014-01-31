(ns lemmy.core
  (use [clojure.string :only [join split]]
       [clojure.core.match :only [match]])
  (:gen-class))


;; make two regex groups, the first is made up of either the numbers 2-10 or J,Q,K or A
;; The second group is one of h, s, d or c
;; The search is constrained to match both the beginning and end of the string?
;; meaning that the max allowable search string is length 3
;; A grouped query in clojure returns a vector, the 0th entry is the total matched string,
;; which we discard. We then utilize the value of the card and the suit in the next sanity
;; check, which is pretty self explanitory
(defn match-card [instr]
  (when-let [[_ value' suit] (re-find #"^([2-9JQKA]|10)([hsdc])$" instr)]
    (let [value (keyword value')]
     (match [suit]
       ["h"] (list value :hearts)
       ["s"] (list value :spades)
       ["c"] (list value :clubs)
       ["d"] (list value :diamonds)
       :else nil))))


;; the parser which produces hands to be analyzed by assess-hand
;; verifies that match-card didn't return a nil for any entry, as that would invalidate the hand
(defn parse-hand [instr]
  (let [result (map match-card (split instr #" "))]
    (if (some #(= nil %) result)
      '(:parse_error "tokenization failed")
      result)))

(defn find-pairs nums hand
  let [] ()
  )

;; the main poker logic function
(defn assess-hand [hand]
  (let [nums (reduce (partial merge-with +)
                     {}
                     (map #(assoc {} (first %) 1)
                          hand))]
    (if (> 5 (count nums))
      (find-pairs nums)
      (find-flush-straight hand))))


(defn -main [& args]
  (println "meh"))

(->> (parse-hand "Ah 3c 2d 10s 10c")
     (assess-hand)
     (filter #(not= 1 (val %)))
     (partition-by val))

(partition-by val (filter #(not= 1 (val %)) (assess-hand  (parse-hand "Ah 3c 2d 10s 10c"))))

(assess-hand (parse-hand "Ah 3c 2d 10s 10c"))

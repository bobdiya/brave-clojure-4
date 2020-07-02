;; Excercises from Chapter 4 of book "Clojure for Brave & True"
;; https://www.braveclojure.com/core-functions-in-depth

;; Copying the code from the book, solutions to the excercise follows A Vampire
;; Data Analysis Program for the FWPD

(ns fwpd.core)
(def filename "suspects.csv")

(def vamp-keys [:name :glitter-index])

(defn str->int
  [str]
  (Integer. str))

(def conversions {:name identity
                  :glitter-index str->int})

(defn convert
  [vamp-key value]
  ((get conversions vamp-key) value))

(convert :glitter-index "3")
;; => 3

(defn parse
  "Convert a CSV into rows of columns"
  [string]
  (map #(clojure.string/split % #",")
       (clojure.string/split string #"\n")))

(parse (slurp filename))
;; => (["Anil" "12" "\r"] ["Nethra" "0" "\r"] ["Kullu" "1" "\r"] ["Mullu" "7" "\r"])

(defn mapify
  "Return a seq of maps like {:name \"Edward Cullen\" :glitter-index 10}"
  [rows]
  (map (fn [unmapped-row]
         (reduce (fn [row-map [vamp-key value]]
                   (assoc row-map vamp-key (convert vamp-key value)))
                 {}
                 (map vector vamp-keys unmapped-row)))
       rows))

(first (mapify (parse (slurp filename))))
;; => {:name "Anil", :glitter-index 12}

(defn glitter-filter
  [minimum-glitter records]
  (filter #(>= (:glitter-index %) minimum-glitter) records))

(glitter-filter 3 (mapify (parse (slurp filename))))
;; => ({:name "Anil", :glitter-index 12} {:name "Mullu", :glitter-index 7})


;; Excercises
;; 1. Turn the result of your glitter filter into a list of names.

(defn glitter-filter-names
  [min-glitter records]
  (map :name (glitter-filter min-glitter records)))

(glitter-filter-names 3 (mapify (parse (slurp filename))))
;; => ("Anil" "Mullu")

;; 2. Write a function, append, which will append a new suspect to your list of
;; suspects.
(def records [{:name "Anil" :glitter-index 2}
              {:name "Pradeep" :glitter-index 12}])

(defn append [suspects suspect-to-add]
  (conj suspects suspect-to-add))

(append records {:name "Anurag" :glitter-index 21})
;; => [{:name "Anil", :glitter-index 2}
;;     {:name "Pradeep", :glitter-index 12}
;;     {:name "Anurag", :glitter-index 21}]

;; 3. Write a function, validate, which will check that :name and :glitter-index
;; are present when you append. The validate function should accept two
;; arguments: a map of keywords to validating functions, similar to conversions,
;; and the record to be validated.

(defn presence [param]
  (if (nil? param)
    false
    true))

(def suspect-validator {:name presence :glitter-index presence})

(defn validate [key-func-map record]
  (reduce
   (fn [result [key val]]
     (and result ((get key-func-map key) (key record))))
   true
   key-func-map))

(validate suspect-validator  {:name "Anil"})
;; => false

(validate suspect-validator  {:name "Anil" :glitter-index 10})
;; => true

;; 4. Write a function that will take your list of maps and convert it back to a
;; CSV string. You’ll need to use the clojure.string/join function.

(defn csv-convert [records]
  (reduce (fn [arg1 arg2] (str arg1 "\n" arg2))
          (map (fn [record]
                 (clojure.string/join "," [(:name record)
                                           (:glitter-index record)]))
          records)))

(csv-convert records);; => "Anil,2\nPradeep,12"

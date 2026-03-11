// Number literal edge cases

// Integers
let zero = 0
let positive = 42
let large = 9_999_999
let max = 2147483647

// Hexadecimal
let hex1 = 0xff
let hex2 = 0xFF
let hex3 = 0xDEAD_BEEF

// Octal
let oct1 = 0o77
let oct2 = 0o777

// Binary
let bin1 = 0b1010
let bin2 = 0b1111_0000

// Floats
let f1 = 0.0
let f2 = 3.14
let f3 = 1.0e10
let f4 = 1.5e-3
let f5 = 1.5E+10
let f6 = 0.123_456

// Negative numbers (unary minus)
let neg1 = -1
let neg2 = -3.14
let neg3 = -0xFF

// BigInt (if supported)
let big = 100n

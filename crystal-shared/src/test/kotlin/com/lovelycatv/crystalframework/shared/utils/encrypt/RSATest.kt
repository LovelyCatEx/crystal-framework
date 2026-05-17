package com.lovelycatv.crystalframework.shared.utils.encrypt

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class RSATest {

    @Test
    fun generateKeyPair() {
        println(keyPair)
    }

    @Test
    fun encryptWithPublicKey() {
        val (pub, priv) = keyPair
        println("Public: $pub")
        println("Private: $priv")
        println("Result: " + RSA.encryptWithPublicKey("CrystalFramework", pub))
    }

    @Test
    fun decryptWithPrivateKey() {
        val (pub, priv) = keyPair
        println("Public: $pub")
        println("Private: $priv")
        val encrypted = RSA.encryptWithPublicKey("CrystalFramework", pub)
        println("Encrypted: $encrypted")
        println("Result: " + RSA.decryptWithPrivateKey(encrypted, priv))
    }

    @Test
    fun specifiedTest() {
        val pub = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAyh9KW278cBXDzx2rf4vMwqkVDv9TraC+TFO0NskpK0bELy9zsdDknLEYTCY9+G60Go11pT0PIehSRXQppGb8OVxs/qb914lprETkMdptEftezTqCfNZmvTSq9gOJz9uRLjcQyk5o41XRc8KSZpbKKiNlgMHEcc/bywSXvjVIjBUPdmyI8KAc0XE/sO3KAkIKPHAQ+HXSgHAqfYHWJ/vQK/0mqLPD+2JlfnB0yPaJr+ALAAtKYYgCxoT1AOQ42+uR2V91QWGSaaZrWM0DU+gaDMcZd0XMUr6iWPg95nQlwn2KRhxgiZt7dxCVv5KlvZQmGC9DEWJlu7XC897O8EMsrQIDAQAB"
        val priv = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQDKH0pbbvxwFcPPHat/i8zCqRUO/1OtoL5MU7Q2ySkrRsQvL3Ox0OScsRhMJj34brQajXWlPQ8h6FJFdCmkZvw5XGz+pv3XiWmsROQx2m0R+17NOoJ81ma9NKr2A4nP25EuNxDKTmjjVdFzwpJmlsoqI2WAwcRxz9vLBJe+NUiMFQ92bIjwoBzRcT+w7coCQgo8cBD4ddKAcCp9gdYn+9Ar/Saos8P7YmV+cHTI9omv4AsAC0phiALGhPUA5Djb65HZX3VBYZJppmtYzQNT6BoMxxl3RcxSvqJY+D3mdCXCfYpGHGCJm3t3EJW/kqW9lCYYL0MRYmW7tcLz3s7wQyytAgMBAAECggEAJRxjqbASF35XinK3J18CxIcI/8zvj0ShTCBeSWTb8z82DDfZNT/WidKWVHz9Cd1BolJw1FsGqUIfwPLYJoBmNR38QTFmECoBxgiMIX8qYY/W0zW3+bXf/UNrVGpH5ms5F9QyeAK3jW4XW/rQqJdtHRmMrHxnVkJE59y7A8iCvn1Xu13cO0IcZW0qyzdGQnrVgmctZXJ+JtQiXuSZvF8K7LpJhHSzpdwhdtbKnbEIYBqHEBUcGmPd54QkvM2bYQ6KrWTir3yhqNeJgMEt7tIldDEUKQAug8FRAKoSaK7QLh6rcwXLtrnwcZubUl2CcP0G+J8QcG+M8X+bxgfY+FIjPQKBgQDyXEi0Mg5eyqEHl15LsKTUvOn0UmbVgFbQGQqEUNRpvuh404T+H0Mgw+hhvEsBZ2hS2TqoZLib1jEAaUvzMqeiiPU1nUHsPa9IFISBNIPPPaOdBs8awuk1CZLkfHK0N+BDXyyu/LMn+JLEecEzhNCsoPJ2sV0DWTFDJlbYHedpdwKBgQDVf0oHJFc+bhc1Y8l1DFddK6+60FAOJBx569ExRO/NfHqvxjNYQuJffjZDVURpboJU+gcdGCYiILongQPP/01t1j41QEYsuZZ1JbfTKI89gVNDCcNFotB/EzUz6Wp1gABc7ZcRd67Auoya36OEl7QCL28nkdNb8AE02/waeYij+wKBgFG4UEOCRVotFNMMdR0seMfK01AwoMAIwDz21Jb88kKdvqFPIR7Wx7m6lRaveNMGXSTiXmb9X0oclPVAi/167Q9fAt+LIPlZa0rwsCUo4fD1JcAC1b5+Tkg17DPiyoJ9/L56zwRJ7NbjeAmLzFtkw0ASHO9sqqJAZt3vhYTlwGvNAoGACf9s+wqjn0POiGyE2ZtbV/ycvRXWOeNNBhSZ9kGxLSFAKY0RiHeRXEFvzxVmH73SEqKmxKEF4CFCNmlfQVVGTfdzcFDzOFL0jcXXZucGVi4BNSl24ILwWpMOscCjgJ8e5TsnEOVW5KWXwgTpBmOIRXXtMaAtkqeZlt0aYC8CzBMCgYBE5reaqEs5W9eOwksgUvhtJtaFnSNUtsjiUfonffRqh6O5OQ+7wkvKRx0xV1sUMU0aCPG96Po/yWJzV3BCd3iQNfSqzu3OE3bi0R2gZuv1Q0B469PKtk90er+a8qRHGIqbiG5TZahppdhpOivoumTNQnwVfYoYrFX6Dmy2vkndHA=="
        println("Public: $pub")
        println("Private: $priv")
        val encrypted = RSA.encryptWithPublicKey("CrystalFramework", pub)
        println("Encrypted: $encrypted")
        println("Result: " + RSA.decryptWithPrivateKey(encrypted, priv))
    }

    companion object {
        lateinit var keyPair: Pair<String, String>

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            keyPair = RSA.generateKeyPair()
        }
    }

}
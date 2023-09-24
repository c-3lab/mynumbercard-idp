let { getMobileOS } = require('../login');
const { signatureNonce } = require('../login');

test('PCで利用した場合', () => {
    expect(getMobileOS()).toBe("other");
});

test('Androidで利用した場合', () => {
    getMobileOS = jest.fn(() => {
        var userAgent = "Android";
        if (userAgent.match(/Android/))
            return 'Android';

        if (userAgent.match(/iPad|iPhone|iPod/))
            return 'iOS';

        // AndroidまたはiOSではない
        return 'other';
    });
    expect(getMobileOS()).toBe("Android");
});

test('iOSで利用した場合', () => {
    getMobileOS = jest.fn(() => {
        var userAgent = "iPad";
        if (userAgent.match(/Android/))
            return 'Android';

        if (userAgent.match(/iPad|iPhone|iPod/))
            return 'iOS';

        // AndroidまたはiOSではない
        return 'other';
    });
    expect(getMobileOS()).toBe("iOS");

    getMobileOS = jest.fn(() => {
        var userAgent = "iPhone";
        if (userAgent.match(/Android/))
            return 'Android';

        if (userAgent.match(/iPad|iPhone|iPod/))
            return 'iOS';

        // AndroidまたはiOSではない
        return 'other';
    });
    expect(getMobileOS()).toBe("iOS");

    getMobileOS = jest.fn(() => {
        var userAgent = "iPod";
        if (userAgent.match(/Android/))
            return 'Android';

        if (userAgent.match(/iPad|iPhone|iPod/))
            return 'iOS';

        // AndroidまたはiOSではない
        return 'other';
    });
    expect(getMobileOS()).toBe("iOS");
});

test('nonce署名', () => {
    expect(signatureNonce("a94fc22f-3f29-429c-839e-cc6185fb15b4", `-----BEGIN PRIVATE KEY-----
    MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCrkhMBdU5quaCi
    eVGryiCszmWXCMJ3MUsdQpxZMa+cll2C7+xQcN2uKy0+HXUEmCbc/Sr5Qat/acpM
    2CFhXf46X0HREcH7LLtmnuRE/LEXYAeOZqUdAEcWaljE6guNqfrHoecmCWKhCQEH
    5kG5nn/usE5i/55acSwegkGB1SWXovGcgXHISV4SQFaaWSP30xoKrVkoyp8ob05W
    a7bCt3IBQ0bIHF8OlSF9vlPIErXXQpk2Aj/vRZ5g2Cp5ttgrruQPv7jou7dKhadj
    6sp5nhKHupItXHkhVkO9sNqfnybB8LM+55iXkuFcw22Hzx1kSuESjauogK/bHRqu
    ovF8ImL7AgMBAAECggEAAlLTncAY7LH8gdsALc+Zn7ynUKoVhWRMe3b9POnJoz4e
    mbdwJmqiJzIwp9m5RwELxQ+3F+E80wDWvtNpIV8nAKbLcniHR46s7GmFQU+2dK8w
    6HrtjE9JyNtTDumJ5CYFde7Sx5h8/uf/cTvrMr1ewMAtFRCDo65lsALtvnKs7WuX
    G6cvVGSfbxXSyN3CaRqnuUrjUv0QmCAcSh8ip3rH5K4BbirH43sSouap7/AyJUZ4
    qwkdicXtkRgUzgf3wayXm+mqVtPzq3xiwOV5XKgAAISW1pKd2TFJ7bRHJ55RV5ZR
    IL0QSNY90JvGo6dI+9PVAkiu42WfMajiuNaEhW/NVQKBgQDfIXJWfIzcRcjKFPFY
    5ghyiLyWsA3Ly1zkYZCepSFSrcC7deLAsLtpaxl2nQY0485v0oQp7xoLU3nBhXf8
    F4SQ7zxb/yMgvXtoacd1dY2xObU6a1mLd8+rLrLZyoTlulwgdw0bTutXryj5322t
    Pg1UBlbgDoGJoqwZ97pg8DwOhwKBgQDE2DrLSzQc1w6mVZikDnSm7jzhSzJ1vBjn
    yH8mBqEPDHaG/Yv+8l/h/oybM4bdz+mOJ/5RpcGLm3iBIolSVWMpwhQH4yDtPkJD
    GczRjw97/L9TR0QAH7avEtZ/h7wAMJkr+9ChZaZTOvChKsgx2mucZG87rD+iLk7f
    aWzeAvuQ7QKBgQDNhLUXzCjCStSirZ+PTnHBbQinkwliCLJK+UKHnXmP+dJn2DE/
    Yol8k9UkV4V7+cgPX0u0hDLs87pV8WWOoOrNxE1IPzxPz7d3eNbq7ZfRAWJXqMEV
    oQl6lszNpae7IWfxzzYXTR2f6gpH/O+8fkcweZZTUYknCNqUIIVe/GeUyQKBgAfn
    Al089KefvpfQ4eLcLbSx5cHJqgnt9jX/55LYO0kGYA/FSASBKHZDKNPmOqjW+vJZ
    Cq2ucNmw8SKGB2tFwEhh4Tn71UuQ70oQVBepnZ6FpAys1L8IhuX6ZKAUC+kaClwo
    rNR95zJ6METYGddehGlv4sNLg/Z303TGn7c9+Wl1AoGAShuRMIGsmEdHc5irKJLu
    fyfbjdzAzVTj3WwqetHwr92dWmhxOvyGtrrf4lWkvIOrMJOoSBeg4y4MjMNq7x0l
    zViNoiFypHanZ5Uo2cFrhG3mYlkk37h3+IX0Y2EYYPfif4O8vSPZoY8rSlECP778
    0vzdJqM8NkPbPms056wJzVE=
    -----END PRIVATE KEY-----`)).toBe("eRCFqdZYhnQFKyFy+an/Dfiwe/Km+IakN+sapcQXnD2e6cmKthxZzEb9bEbVb4ZzqBwTW2kAEEbygKqvvXv6C8u3y/i2nupnJ7TDCcftKil5OGDzfJBigcpiemvS5VjquCJH7powExUjBWR6FZQ5Y0pPmyVT7xgz3jQ6kCJu+1DnwpPk3wljfR4FUgGKd3UHfCaR39/BorrOwDKAK6SRU52zxuDNZ+odboBprODyvDKwoRL4/6A52h/u3Xlwl+OVpVns2LRUR1MycWo3SzZZAu2osMO9K8aaCj/cnljPkx+9e3cXC2WeJlKXUGhGFgXuHogjPZUYr0Pw4ZGk9JTE0w==");
});

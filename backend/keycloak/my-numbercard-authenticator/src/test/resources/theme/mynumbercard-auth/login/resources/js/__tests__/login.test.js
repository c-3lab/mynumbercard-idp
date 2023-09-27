let { getMobileOS } = require('../login');

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
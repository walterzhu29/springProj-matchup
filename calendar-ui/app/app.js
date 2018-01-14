var app = angular.module('mainform', []);
app.value("timeFormat", 'yyyy-MM-dd HH:mm:ss');
app.value("timeZoneDft", 'EST')
app.controller('formController', function ($scope, $http, timeFormat, timeZoneDft) {
    $scope.timeMin = timeFormat;
    $scope.timeMax = timeFormat;
    $scope.timeZone = timeZoneDft;
    $scope.matchup = function() {
        $http({
            method: 'GET',
            url: 'http://localhost:8080/matchup/match-up',
            // params: {
            //     calendarId1: 'u9udoeo3tukabp92990sbe3c2s@group.calendar.google.com',
            //     calendarId2: 'v2qv94guse9j2f79iq5tgiqm0s@group.calendar.google.com',
            //     timeMin: '2018-01-08 06:00:00',
            //     timeMax: '2018-01-08 10:00:00',
            //     timeZone: 'EST'
            // }
            params: {
                calendarId1: $scope.calendarId1,
                calendarId2: $scope.calendarId2,
                timeMin: $scope.timeMin,
                timeMax: $scope.timeMax,
                timeZone: $scope.timeZone
            }
        }).success(function (data) {
            console.log(data);
            $scope.Match_Result = data;
        }).error(function (data, status, headers, config) {
            alert("ERROR!")
        });
    }
});

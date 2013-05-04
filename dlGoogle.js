var fs = require('fs')
var jsdom = require('jsdom');
//var $ = require('jquery');
var $ = fs.readFileSync("./jquery.js").toString();
var content = fs.readFileSync("inputHtml.html").toString();
var _ = require('underscore');

var ngramsNeeded =5;
//console.log(content);
console.log("read File Finish");
var dlCommands = [];
jsdom.env({
    html : content,
    src : [$],
    // scripts : ["http://code.jquery.com/jquery.js"],
    //src:[$.toString()],
    done : function(err, window) {
        var $ = window.$;
        var ngramList = $("#container h1").filter(function() {
            //may want to use $.trim in here
            // return $(this).text() == 'Chinese (simplified)';
                        return $(this).text() == 'English';
        }).nextAll("p");
       var links = ngramList.slice(0,ngramsNeeded).children("a");
        
        //first is n-gram , etc
        //after 5 way be another language
        console.log("links size" + links.size());
        links.each(function(i) {
            var link = $(this).attr("href");
            console.log('[LINK]' + link);
            var last = link.lastIndexOf("-") + 1;
            var key = link.slice(last, -3);
            //last index before .gz
            console.log('key:' + key);
            var command = 'wget  -O tmp.gz ' + link + ' && gzip -d tmp.gz && mv tmp ' + key;

            dlCommands.push(command);
        });
        console.log("all commands");
        console.log(dlCommands);
        var stream = fs.createWriteStream("commands.sh");
        stream.once('open', function(fd) {
            $.each(dlCommands, function(i, data) {
                stream.write(data+"\n");
            });
            stream.end();
        });

    }
});

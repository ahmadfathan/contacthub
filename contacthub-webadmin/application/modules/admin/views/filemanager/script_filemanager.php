<script type="text/javascript">
$(document).ready(function () {
	$('.iframe-btn').fancybox({
		'width'	: 880,
		'height'	: 570,
		'type'	: 'iframe',
		'autoScale'   : false
	});
	//
	// Handles message from ResponsiveFilemanager
	//
	function OnMessage(e){
	  var event = e.originalEvent;
	   // Make sure the sender of the event is trusted
	   if(event.data.sender === 'responsivefilemanager'){
	      if(event.data.field_id){
	      	var fieldID=event.data.field_id;
	      	var url=event.data.url;
					$('#'+fieldID).val(url).trigger('change');
					$.fancybox.close();

					// Delete handler of the message from ResponsiveFilemanager
					$(window).off('message', OnMessage);
	      }
	   }
	}

  // Handler for a message from ResponsiveFilemanager
	$('.iframe-btn').on('click',function(){
	  $(window).on('message', OnMessage);
	});



      $('#download-button').on('click', function() {
	    ga('send', 'event', 'button', 'click', 'download-buttons');
      });
      $('.toggle').click(function(){
	    var _this=$(this);
	    $('#'+_this.data('ref')).toggle(200);
	    var i=_this.find('i');
	    if (i.hasClass('icon-plus')) {
		  i.removeClass('icon-plus');
		  i.addClass('icon-minus');
	    }else{
		  i.removeClass('icon-minus');
		  i.addClass('icon-plus');
	    }
      });
});
function show_file_manager(name){
	$("#iframe_file_manager").attr('src',"<?= base_url() ?>/filemanager/dialog.php?type=2&field_id="+name+"\'&fldr='");
	$("#myModal").modal();
}
</script>

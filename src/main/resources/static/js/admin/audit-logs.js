    function handleTimeRangeChange(val) {
        const customBox = document.getElementById('customDateContainer');
        if (val === 'custom') {
            customBox.classList.remove('hidden');
        } else {
            customBox.classList.add('hidden');
            document.getElementById('logStartDate').value = '';
            document.getElementById('logEndDate').value = '';
            document.getElementById('auditFilterForm').submit();
        }
    }

    function clearKeyword() {
        const form = document.getElementById('auditFilterForm');
        const input = form.querySelector('input[name="keyword"]');
        if (input) input.value = '';
        form.submit();
    }
 override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCleanupBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置ActionBar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "清理备份文件"

        // 初始化RecyclerView
        setupRecyclerView()
        // 加载备份文件
        loadBackupFiles()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (actionMode != null) {
                    actionMode?.finish()
                } else {
                    finish()
                }
            }
        })
    }

    private fun setupRecyclerView() {
        adapter = BackupFileAdapter(mutableListOf(), this::onFileLongClick, this::onFileSelected)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@CleanupBackupActivity)
            adapter = this@CleanupBackupActivity.adapter
            addItemDecoration(DividerItemDecoration(this@CleanupBackupActivity, DividerItemDecoration.VERTICAL))

            val swipeController = SwipeController()
            val itemTouchHelper = ItemTouchHelper(swipeController)
            itemTouchHelper.attachToRecyclerView(binding.recyclerView)

            // 添加触摸事件监听器来处理按钮点击
            binding.recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                    // 仅在非多选模式时处理滑动按钮事件
                    if (actionMode == null) {
                        swipeController.handleButtonClick(e, rv)
                    }
                    return false
                }

                override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

                override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
            })

        }

        // 添加快速滚动条
        binding.fastScroller.attachToRecyclerView(binding.recyclerView)
    }

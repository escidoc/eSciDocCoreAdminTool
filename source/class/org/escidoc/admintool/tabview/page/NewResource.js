/**
 * @author CHH
 */
qx.Class.define("org.escidoc.admintool.tabview.page.NewResource", {
			extend : qx.ui.tabview.Page,
			construct : function(id) {
				this.base(arguments, this.title, this.icon);
				this.setLayout(new qx.ui.layout.HBox());
				this.setShowCloseButton(true);

				var container = new qx.ui.container.Composite();
				container.forms = new Array();
				container.setLayout(new qx.ui.layout.VBox());
				this.add(container);
				this.bind("title", this, "label");

				this.form = new qx.ui.form.Form();
				container.forms.push(this.form);

				// name
				var nameTextField = this.createNameTextField();
				this.form.add(nameTextField, "Name", null, "name");
				nameTextField.bind("value", this, "title");

				// login name
				var loginTextField = this.createLoginTextField();
				this.form.add(loginTextField, "Login Name", null, "loginName");

				// // password
				var passwordField = new qx.ui.form.PasswordField();
				passwordField.setRequired(true);
				this.form.add(passwordField, "Password", null, "password");

				// Email
				var emailTextField = new qx.ui.form.TextField();
				this.form.add(emailTextField, "Email",
						null, "email");
				emailTextField.bind("value", this, "email");

				// roles
				var comboBox = new qx.ui.form.ComboBox();
				comboBox.setPlaceholder("Roles");
				this.form.add(comboBox, "Roles", null, "role");
				this.createItems(comboBox);

				// create button
				var createButton = new qx.ui.form.Button("Create");
				this.form.addButton(createButton);

				// binding
				var formController = new qx.data.controller.Form(null,
						this.form);
				var model = formController.createModel();

				// invoke the serialization
				createButton.addListener("execute", function() {
							if (this.form.validate()) {
								alert("You entered: "
										+ qx.util.Serializer.toJson(model)
										+ " and valid.");
							} 
						}, this);

				var formView = new qx.ui.form.renderer.Single(this.form);
				container.add(formView);
				/*
				 * var folderBox = new qx.ui.groupbox.GroupBox("Folder");
				 * folderBox.setLayout(new qx.ui.layout.VBox());
				 * 
				 * container.add(folderBox);
				 */
			},
			statics : {
				ITEM_SIZE : 5,
				DEFAULT_VALIDATOR : null
			},
			properties : {
				title : {
					init : "",
					check : "String",
					nullable : false,
					event : "changeTitle"
				},
				email : {
					init : "",
					check : "String",
					nullable : false,
					event : "changeEmail"
				},
				infrastructureUrl : {
					init : "http://localhost:8080",
					check : "String",
					nullable : false,
					event : "changeInfrastructureUrl"
				},
				startTime : {
					// XML compatible date-time-string
					init : null,
					check : "String",
					nullable : true,
					event : "changeStartTime"
				}
			},
			members : {
				icon : "icon/16/apps/preferences-users.png",
				form : null,
				createNameTextField : function() {
					var nameTextField = new qx.ui.form.TextField(this
							.getTitle());
					nameTextField.setRequired(true);
					nameTextField.setWidth(200);
					return nameTextField;
				},
				createLoginTextField : function() {
					var nameTextField = new qx.ui.form.TextField();
					nameTextField.setRequired(true);
					nameTextField.setWidth(200);
					return nameTextField;
				},
				createItems : function(widget) {
					for (var i = 0; i < this.self(arguments).ITEM_SIZE; i++) {
						var tempItem = new qx.ui.form.ListItem("Roles " + i);
						widget.add(tempItem);
					}
				}
			}
		});